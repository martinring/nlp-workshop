# NLP Workshop

This repository contains the sources from the NLP Workshop.

## Project

The Project under `example` contains two Files `Synonyms` and `Stanford`. They contain the two examples we developed during the workshop. 

`Synonyms` tries to guess POS of words in a sentence and replaces words by potential synonyms.

`Stanford` can recognize simple insults both in nominal and ajective form. It can additionally recognize arbitrary levels of negation.

````
> Peter said, that Martin is dumb
No, Martin is great!
> I heard, that Martin isn't very intelligent.
Yes he is!
> Peter said that Carol is a dingbat.
No, Carol is great!
> I heard something about Martin not beeing a dimwit.
> I heard something about the fact, that Martin isn't smart.
Yes he is!
> Why is Martin so damn dumb?
No, Martin is great!
> Martin is not very smart.
Yes he is!
````

## Links

- [POS Tags](https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html)
- [CoreNLP](http://corenlp.run)
- [Annotators](https://stanfordnlp.github.io/CoreNLP/annotators.html)
- [WordNet](http://wordnetweb.princeton.edu/perl/webwn)
- [IRC](https://tools.ietf.org/html/rfc1459)
- [Dependencies](http://universaldependencies.org/u/dep/index.html)