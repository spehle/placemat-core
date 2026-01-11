#define _POSIX_C_SOURCE 200809L

#include <ctype.h>
#include <errno.h>
#include <jansson.h>
#include <curl/curl.h>
#include <ncurses.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#define MAX_FIELD 256

typedef struct {
    char *data;
    size_t len;
} Buffer;

static size_t write_cb(void *ptr, size_t size, size_t nmemb, void *userdata) {
    size_t n = size * nmemb;
    Buffer *b = (Buffer *)userdata;
    char *p = realloc(b->data, b->len + n + 1);
    if (!p) return 0;
    b->data = p;
    memcpy(b->data + b->len, ptr, n);
    b->len += n;
    b->data[b->len] = '\0';
    return n;
}

static void trim_newline(char *s) {
    if (!s) return;
    size_t n = strlen(s);
    while (n > 0 && (s[n-1] == '\n' || s[n-1] == '\r')) {
        s[n-1] = '\0';
        n--;
    }
}

static void draw_frame(const char *title) {
    clear();
    box(stdscr, 0, 0);
    if (title) {
        mvprintw(0, 2, " %s ", title);
    }
    refresh();
}

static void status_line(const char *msg) {
    int h, w;
    getmaxyx(stdscr, h, w);
    mvhline(h-2, 1, ' ', w-2);
    mvprintw(h-2, 2, "%s", msg ? msg : "");
    refresh();
}

static void read_field(int y, int x, const char *label, char *out, size_t out_sz, bool secret) {
    mvprintw(y, x, "%s", label);
    mvprintw(y+1, x, "> ");
    refresh();

    memset(out, 0, out_sz);

    int ch;
    size_t pos = 0;
    keypad(stdscr, TRUE);
    noecho();
    curs_set(1);

    while ((ch = getch()) != ERR) {
        if (ch == '\n' || ch == '\r') {
            out[pos] = '\0';
            break;
        } else if (ch == KEY_BACKSPACE || ch == 127 || ch == '\b') {
            if (pos > 0) {
                pos--;
                out[pos] = '\0';
                int cx = x + 2 + (int)pos;
                mvaddch(y+1, cx, ' ');
                move(y+1, cx);
            }
        } else if (isprint(ch)) {
            if (pos + 1 < out_sz) {
                out[pos++] = (char)ch;
                int cx = x + 2 + (int)pos - 1;
                mvaddch(y+1, cx, secret ? '*' : ch);
            }
        }
        refresh();
    }

    curs_set(0);
}

static char *json_escape_string(const char *s) {
    // Minimal JSON string escape for username/password
    // (good enough for typical input; jansson handles proper encoding but we build body ourselves)
    size_t n = strlen(s);
    // Worst-case expand ~2x + quotes
    char *buf = malloc(n * 2 + 3);
    if (!buf) return NULL;
    char *p = buf;
    *p++ = '"';
    for (size_t i = 0; i < n; i++) {
        unsigned char c = (unsigned char)s[i];
        switch (c) {
            case '\\': *p++='\\'; *p++='\\'; break;
            case '"':  *p++='\\'; *p++='"';  break;
            case '\n': *p++='\\'; *p++='n';  break;
            case '\r': *p++='\\'; *p++='r';  break;
            case '\t': *p++='\\'; *p++='t';  break;
            default:
                if (c < 0x20) {
                    // control chars -> skip (or encode as \u00XX if you want)
                } else {
                    *p++ = (char)c;
                }
        }
    }
    *p++ = '"';
    *p = '\0';
    return buf;
}

static bool http_post_login(const char *base_url, const char *lang,
                            const char *username, const char *password,
                            long *http_code_out, char **response_out) {
    *response_out = NULL;
    *http_code_out = 0;

    CURL *curl = curl_easy_init();
    if (!curl) return false;

    Buffer buf = {0};

    char url[512];
    snprintf(url, sizeof(url), "%s/api/auth/login", base_url);

    char *u = json_escape_string(username);
    char *p = json_escape_string(password);
    if (!u || !p) {
        free(u); free(p);
        curl_easy_cleanup(curl);
        return false;
    }

    char body[1024];
    snprintf(body, sizeof(body), "{\"username\":%s,\"password\":%s}", u, p);
    free(u); free(p);

    struct curl_slist *headers = NULL;
    headers = curl_slist_append(headers, "Content-Type: application/json");
    if (lang && lang[0]) {
        char h[128];
        snprintf(h, sizeof(h), "Accept-Language: %s", lang);
        headers = curl_slist_append(headers, h);
    }

    curl_easy_setopt(curl, CURLOPT_URL, url);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, body);

    // For dev: verbose off, but you can toggle if needed
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_cb);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &buf);

    CURLcode res = curl_easy_perform(curl);
    if (res != CURLE_OK) {
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
        free(buf.data);
        return false;
    }

    long code = 0;
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &code);

    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);

    *http_code_out = code;
    *response_out = buf.data ? buf.data : strdup("");
    return true;
}

static bool http_get_me(const char *base_url, const char *lang,
                        const char *token, long *http_code_out, char **response_out) {
    *response_out = NULL;
    *http_code_out = 0;

    CURL *curl = curl_easy_init();
    if (!curl) return false;

    Buffer buf = {0};

    char url[512];
    snprintf(url, sizeof(url), "%s/api/auth/me", base_url);

    struct curl_slist *headers = NULL;
    if (token && token[0]) {
        char auth[2048];
        snprintf(auth, sizeof(auth), "Authorization: Bearer %s", token);
        headers = curl_slist_append(headers, auth);
    }
    if (lang && lang[0]) {
        char h[128];
        snprintf(h, sizeof(h), "Accept-Language: %s", lang);
        headers = curl_slist_append(headers, h);
    }

    curl_easy_setopt(curl, CURLOPT_URL, url);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_HTTPGET, 1L);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_cb);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &buf);

    CURLcode res = curl_easy_perform(curl);
    if (res != CURLE_OK) {
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
        free(buf.data);
        return false;
    }

    long code = 0;
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &code);

    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);

    *http_code_out = code;
    *response_out = buf.data ? buf.data : strdup("");
    return true;
}

static char *parse_token_from_login_response(const char *json_str) {
    if (!json_str) return NULL;
    json_error_t err;
    json_t *root = json_loads(json_str, 0, &err);
    if (!root) return NULL;

    json_t *token = json_object_get(root, "token");
    if (!json_is_string(token)) {
        json_decref(root);
        return NULL;
    }

    const char *t = json_string_value(token);
    char *out = strdup(t ? t : "");
    json_decref(root);
    return out;
}

static void ensure_dir(const char *path) {
    (void)mkdir(path, 0700);
}

static char *token_path(void) {
    const char *home = getenv("HOME");
    if (!home) home = ".";
    char *p = malloc(strlen(home) + 64);
    if (!p) return NULL;
    snprintf(p, strlen(home) + 64, "%s/.config/placemat", home);
    ensure_dir(p);

    char *f = malloc(strlen(home) + 80);
    if (!f) { free(p); return NULL; }
    snprintf(f, strlen(home) + 80, "%s/.config/placemat/token", home);
    free(p);
    return f;
}

static void save_token(const char *token) {
    char *path = token_path();
    if (!path) return;

    FILE *fp = fopen(path, "w");
    if (fp) {
        fprintf(fp, "%s\n", token ? token : "");
        fclose(fp);
        chmod(path, 0600);
    }
    free(path);
}

static char *load_token(void) {
    char *path = token_path();
    if (!path) return NULL;

    FILE *fp = fopen(path, "r");
    free(path);
    if (!fp) return NULL;

    char buf[4096];
    if (!fgets(buf, sizeof(buf), fp)) {
        fclose(fp);
        return NULL;
    }
    fclose(fp);
    trim_newline(buf);
    if (buf[0] == '\0') return NULL;
    return strdup(buf);
}

int main(int argc, char **argv) {
    const char *base_url = "http://localhost:8080";
    const char *lang = "en";

    if (argc >= 2) base_url = argv[1];
    if (argc >= 3) lang = argv[2];

    curl_global_init(CURL_GLOBAL_DEFAULT);

    initscr();
    cbreak();
    noecho();
    curs_set(0);

    draw_frame("placemat TUI - login");
    mvprintw(2, 2, "Backend: %s", base_url);
    mvprintw(3, 2, "Language: %s (Accept-Language)", lang);
    mvprintw(5, 2, "F1: login   F2: test /me   F3: show saved token   F10: quit");
    refresh();

    char *token = load_token();

    int ch;
    keypad(stdscr, TRUE);
    while ((ch = getch()) != ERR) {
        if (ch == KEY_F(8)) break;

        if (ch == KEY_F(1)) {
            draw_frame("Login");
            mvprintw(2, 2, "Backend: %s", base_url);
            mvprintw(3, 2, "Language: %s", lang);
            mvprintw(5, 2, "Press Enter after each field.");
            refresh();

            char username[MAX_FIELD];
            char password[MAX_FIELD];

            read_field(7, 2, "Username", username, sizeof(username), false);
            read_field(10, 2, "Password", password, sizeof(password), true);

            status_line("Logging in...");

            long code = 0;
            char *resp = NULL;
            bool ok = http_post_login(base_url, lang, username, password, &code, &resp);

            // clear password buffer ASAP
            memset(password, 0, sizeof(password));

            if (!ok) {
                status_line("HTTP error (curl). Check backend URL / connectivity.");
                free(resp);
                continue;
            }

            if (code == 200) {
                char *new_token = parse_token_from_login_response(resp);
                if (new_token && new_token[0]) {
                    free(token);
                    token = new_token;
                    save_token(token);
                    status_line("Login OK. Token saved to ~/.config/placemat/token");
                } else {
                    status_line("Login OK but could not parse token from response.");
                    free(new_token);
                }
            } else {
                // show server error body if present
                int h, w;
                getmaxyx(stdscr, h, w);
                mvhline(13, 1, ' ', w-2);
                mvprintw(13, 2, "HTTP %ld: %s", code, (resp && resp[0]) ? resp : "(no body)");
                status_line("Login failed.");
            }

            free(resp);

        } else if (ch == KEY_F(2)) {
            draw_frame("Test /api/auth/me");
            if (!token) {
                status_line("No token saved. Press F1 to login first.");
                continue;
            }

            status_line("Calling /api/auth/me...");
            long code = 0;
            char *resp = NULL;
            bool ok = http_get_me(base_url, lang, token, &code, &resp);

            if (!ok) {
                status_line("HTTP error (curl).");
                free(resp);
                continue;
            }

            int h, w;
            getmaxyx(stdscr, h, w);
            mvprintw(2, 2, "HTTP %ld", code);
            mvprintw(4, 2, "Response:");
            // crude wrap
            int y = 6, x = 2;
            for (const char *p = resp ? resp : ""; *p && y < h-3; p++) {
                if (*p == '\n' || x >= w-3) { y++; x = 2; continue; }
                mvaddch(y, x++, *p);
            }
            status_line("Press any key to return.");
            flushinp();
            getch();

            free(resp);

        } else if (ch == KEY_F(3)) {
            draw_frame("Saved token");
            if (!token) {
                mvprintw(2, 2, "(no token saved)");
            } else {
                mvprintw(2, 2, "Token (first 80 chars):");
                mvprintw(4, 2, "%.80s", token);
            }
            status_line("Press any key to return.");
            flushinp();
            getch();

        } else {
            // redraw home
            draw_frame("placemat TUI - login");
            mvprintw(2, 2, "Backend: %s", base_url);
            mvprintw(3, 2, "Language: %s (Accept-Language)", lang);
            mvprintw(5, 2, "F1: login   F2: test /me   F3: show saved token   F10: quit");
            if (token) {
                mvprintw(7, 2, "Token: saved (%.16s...)", token);
            } else {
                mvprintw(7, 2, "Token: not saved");
            }
            refresh();
        }
    }

    endwin();
    free(token);
    curl_global_cleanup();
    return 0;
}
