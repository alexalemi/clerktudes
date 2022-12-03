.PHONY: all serve clean test


all:
	clj -X:clerk

test:
	clj -X:nextjournal/clerk :browse true

serve: all
	python -m http.server 1313

clean:
	rm -rf _data
	rm *.html
