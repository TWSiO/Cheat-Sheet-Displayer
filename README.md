# Cheat Sheet Displayer

Just a simple web app to view information from various cheat sheets. Built to make quick referencing of information easy.

Not intended or built to be super reusable, but you can try. Could be useful for inspiration on other Reagent projects as well.

You can see it in action on my site [here](https://thiswebsiteis.online/cheat_sheet).

## Notes

One warning is that you shouldn't use .edn files with the shadow-cljs test server. Write the edn text in a .txt file instead. The short version of the reason is that it's a problem because the shadow-cljs test server serves .edn files differently than most other http servers, and the code is written for use with other servers which causes a problem.

The long version is that the test server serves .edn files as something like `Content-Type: application/edn` which allows cljs-http to parse them directly to clojure values. *However*, most other basic static HTTP file servers do not serve them as `application/edn` since that's not actually an IANA recognized media type. It sends it instead as `application/octet-stream` which is interpreted as text by cljs-http which then needs to be parsed from the edn string to clojure values.

### Development mode
```
npm install
npx shadow-cljs watch app
```
start a ClojureScript REPL
```
npx shadow-cljs browser-repl
```
### Building for production

```
npx shadow-cljs release app
```
