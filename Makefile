.PHONY: all serve


all:
	clj -X:clerk

serve: all
	python -m http.server 1313
