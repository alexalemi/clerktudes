.PHONY: all serve clean


all:
	clj -X:clerk

serve: all
	python -m http.server 1313

clean:
	rm -rf _data
	rm *.html
