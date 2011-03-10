# Scalosure

Scalosure is an experimental set of tools for creating JavaScript applications with the Scala language.  My primary goal is to support a large subset of Scala's features and libraries that translate well into JavaScript and are relevant to the browser environment.

This is a fork/rewrite of another project trying to achieve a similar thing [S2JS](https://github.com/alvaroc1/s2js) and as such, contains some shared code.

# Components

* Compiler: This is a Scala compiler plug-in which attempts to translate the AST into JavaScript with heavy dependencies on Google's [Closure](http://code.google.com/closure/) tools.
* Adapters: Theres are a set of Scala classes that provide adapters to other JavaScript APIs (mostly Google Closure)
* Tools: Currently only has a Servlet, which can generate the necessary dependency file in order to load scripts into the browser
* Examples: what it sounds like :-)

# Dependencies

* [SBT](https://github.com/harrah/xsbt)
* [Google Closure](http://code.google.com/closure/)

# Getting started (Coming soon)

Since the project in incomplete in it's current state you are on your own.  * Clone the repository

