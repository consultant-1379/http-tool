
## JavaDocs

* [JavaDocs](	https://taf.seli.wh.rnd.internal.ericsson.com/http-tool/apidocs)

## Contents

<!-- MACRO{toc} -->

## What is the Http Tool?

Http Tool provides an API which allows executing HTTP requests and retrieve response data such as headers, body, cookies etc. Http Tool may be used for testing REST interfaces.

## What is REST?

[Representational State Transfer (REST)](http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm) is an architectural style for designing network applications. The idea is that rather than using 
complex mechanisms such as CORBA, RPC or SOAP to connect between machines, simple HTTP/HTTPS is used to make calls.

## Getting started

To create and configure Http Tool use `BasicHttpToolBuilder.newBuilder(serverHost)` method. See [Examples](#Examples).

<div class="note"></div>
`BasicHttpToolBuilder.newBuilder()` accepts url, ip or ipv6 address. If you need to construct Http Tool using `com.ericsson.cifwk.taf.data.Host` objects, refer to [using tool with TAF](#host) section.

Method returns builder object which allows configuring additional parameters, such as use of HTTPS, follow redirect behavior and set SSL key and certificate. To construct basic Http Tool use `build()` method:

<!-- MACRO{snippet|id=BUILDER|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

To construct Http Tool with additional parameters use builder configuration methods, for example:

<!-- MACRO{snippet|id=EXAMPLE_0|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->
<!-- Adding a comment line for E2C FEM testing -->

Http Tool represents browser instance, i.e. one instance stores [cookies](#Managing_Cookies) during the session.

Once Http Tool is created, requests may be executed by methods with corresponding names (i.e. `get(url), post(url)` e.t.c) using URL relative to host path. For example, to GET URL `http://192.168.1.1/test/` (where `192.168.1.1` is defined in the host) use:

<!-- MACRO{snippet|id=EXAMPLE_1|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

<div class="note danger"></div>
It is required to call `HttpTool.close()` method when it is not needed anymore to reduce memory consumption.

To execute request with additional parameters use tool `request()` method which returns request builder. For example, to call URL with query parameters `param1=value1` and `param2=value2` (`http://192.168.1.1/test/?param1=value1&param2=value2`) use:

<!-- MACRO{snippet|id=EXAMPLE_2|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

`queryParam("name", "value1", "value2", "value3")` will 
execute a request to the following URL: `http://192.168.1.1/test/?name=value1&name=value2&name=value3`.

Request has multiple options, for example, it is possible to add header to request (`header(name, value)`), set content type (`contentType(contentType)`), and set request timeout `timeout(sec)`.

## Get HTTP tool

You can get HTTP tool by adding maven dependency

```xml
<dependency>
    <groupId>com.ericsson.de</groupId>
    <artifactId>http-tool</artifactId>
    <version>${version}</version>
</dependency>
```

## HttpResponse

Each of the `post(url), get(url), put(url), head(url), delete(url)` methods returns `HttpResponse` bean which contains response headers, code, body and cookies state. 
This bean is intended for test assertions. Example of simple test case using Http Tool:

<!-- MACRO{snippet|id=EXAMPLE_3|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

## Download File

If the request returns a file (text or binary), you can access it using the `getContent()` method in `HttpResponse`:

<!-- MACRO{snippet|id=EXAMPLE_4|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

## Post XML/Json

To post XML or JSON use `body(data)` method which accepts `String` or `InputStream`:

<!-- MACRO{snippet|id=EXAMPLE_5|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

## Post Form or File

To post form data [Multipart](https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html) use `body(name, data)` method with two arguments: name and data. 
In this case data may be also be `String` or `InputStream`. To post file use `file(name, data)` method.

<!-- MACRO{snippet|id=EXAMPLE_6|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

In this example `multipart/form-data` has been used, because binary file was added 
to request. If you create request body which contains only text (`String`), data request type will be `application/x-www-form-urlencoded`. It is possible to 
explicitly set `multipart/form-data` for request with text body:

<!-- MACRO{snippet|id=EXAMPLE_7|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

## Put Form

To build a PUT request using the Http Tool follow the same structure as a POST request.

<!-- MACRO{snippet|id=EXAMPLE_8|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

## Managing Cookies

For test assertions, cookies are available in [HttpResponse](#HttpResponse) object: `response.getCookies()`

As Http Tool represents browser instance, it stores cookies for the whole session. Cookies can be accessed using `tool.getCookies()` and removed using `tool.clearCookies()`.

Cookies can be managed with `tool.clearCookies()` and `tool.addCookie(COOKIE_NAME, COOKIE_VALUE)` methods.

## Authentication

Currently Http Tool supports authentication with SSL key and certificate in `.pem` format. Certificate and key may be set via Http Tool Builder 
method `setSslKeyAndCert(key, cert)` which accepts as argument either path to key and certificate in file system, or `java.io.Reader` for these objects.
Additionally, `useHttpsIfProvided` must be set to `true` to enable HTTPS. To ignore SSL errors with partially valid certificates or self signed 
certificates, `trustSslCertificates` flag should be also set to `true`

<!-- MACRO{snippet|id=EXAMPLE_9|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

The following features are available with Mutual SSL Authentication:

### Trust the server (With no client validation)

<!-- MACRO{snippet|id=SSL_1|file=http-tool/src/test/java/com/ericsson/de/tools/http/MutualSSLTest.java} -->

### Server validation

<!-- MACRO{snippet|id=SSL_2|file=http-tool/src/test/java/com/ericsson/de/tools/http/MutualSSLTest.java} -->

### Client validation

<!-- MACRO{snippet|id=SSL_3|file=http-tool/src/test/java/com/ericsson/de/tools/http/MutualSSLTest.java} -->

### Multiple certificates

<!-- MACRO{snippet|id=SSL_4|file=http-tool/src/test/java/com/ericsson/de/tools/http/MutualSSLTest.java} -->

### Java Keystore (Contains multiple certificates)

<!-- MACRO{snippet|id=SSL_5|file=http-tool/src/test/java/com/ericsson/de/tools/http/MutualSSLTest.java} -->

### Disable hostname verification

<!-- MACRO{snippet|id=SSL_6|file=http-tool/src/test/java/com/ericsson/de/tools/http/MutualSSLTest.java} -->

### Basic authentication

Additionally, Http Tool supports HTTP Basic Authentication for each request via `authenticate(username, password)` method of request builder:

<!-- MACRO{snippet|id=EXAMPLE_10|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

## Examples

For examples refer to:

* Git Repo: `git clone ssh://<signum>@gerrit.ericsson.se:29418/OSS/com.ericsson.de/http-tool`
* Path to Http Example: `http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java`

### TAF Examples

Using Http Tool with TAF Framework Examples can be found in Git at the below location:

* Git Repo: `git clone ssh://<signum>@gerrit.ericsson.se:29418/OSS/com.ericsson.cifwk/ERICtaf_examples`
* Path to Http Example: `/HttpTool-example`

Please refer to [Http Examples](http://confluence-nam.lmera.ericsson.se/display/TAF/Http%28Rest%29+example) for the description of the examples.

### Workshops

[Http Tool Workshop](https://confluence-nam.lmera.ericsson.se/display/TAF/TAF+Http+Tool+Workshop)

<a id="host"></a>
## Using Http Tool With TAF Host Object

### Request using HTTP or HTTPS

`HttpToolBuilder` takes as input [Host](	https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/handlers_and_executors/data_handler.html) object that may contain ports defined for both HTTP and HTTPS. If `useHttpsIfProvided` in `HttpToolBuilder` is set to `true` and `Host` object contains port for HTTPS, then request will be made using HTTPS. If no port defined, request will be made using HTTP.

### Request using IPv4, IPv6 or Hostname

[Host](	https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/handlers_and_executors/data_handler.html) may have fields `hostname`, `ip` and `ipv6` defined. In `HttpToolBuilder` it's possible to define preferred way to access target host:

* With `useHostnameIfProvided` set to `true`: if `Host` object has `hostname` defined it will be accessed via hostname. Else it will be accessed via IPv4 address.
* With `useIpv6IfProvided` set to `true`: if `Host` object has `ipv6` defined it will be accessed via IPv6 address. Else it will be accessed via IPv4 address.

**NOTE** If `useIpv6IfProvided` is set, then target will be accessed via IPv6 address from `Host` object
(taken from host properties/json). If requirement is to test that `hostname` is resolvable via IPv6, 
consider using [Custom DNS Resolver](#IPV6_ONLY_DNS_Resolver)

## Advanced Http Tool Usage

Advanced users may want access to `HttpToolImpl` 
and setting custom `ResponseHandler`. `ResponseHandler` should implement corresponding interface, which 
accepts `com.ericsson.cifwk.taf.tools.http.HttpResponse`. It is up to user to build `HttpResponse`.

<!-- MACRO{snippet|id=EXAMPLE_13|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

### Custom DNS Resolver

To override default DNS lookup it's possible to set custom DNS Resolver. This might be used when default Java DNS Lookup
 `java.net.InetAddress.getAllByName(java.lang.String)` does not fit the requirement, due to [caching](https://docs.oracle.com/javase/7/docs/api/java/net/InetAddress.html) or other specifics.

Custom DNS resolver should implement `com.ericsson.cifwk.taf.tools.http.DnsResolver`.
 
For example, to override hostname `custom.local` resolution to `127.0.0.1`:
 
<!-- MACRO{snippet|id=EXAMPLE_11|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

### IPV6_ONLY DNS Resolver

By default, if IPv6 address lookup or connection fails, Http Tool will try to connect via IPv4. To ensure that target host has IPv6 configured properly,
it's possible to use `com.ericsson.cifwk.taf.tools.http.DnsResolver#IPV6_ONLY` so in case of issues with IPv6 connection Http Tool will throw
exception instead of fallback to IPv4.

Example: 

<!-- MACRO{snippet|id=EXAMPLE_12|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

### Ignore response body

If Http Tool is used intensively (e.g. in performance test) and response body is not essential for the test it is possible to reduce memory consumption by ignoring response body. 

<!-- MACRO{snippet|id=EXAMPLE_14|file=http-tool/src/test/java/com/ericsson/de/tools/http/examples/HttpToolExamples.java} -->

This will result that response object body will be `null` regardless actual response. 

### Configure connection timeouts

When a new instance of `HttpTool` is created, there are default values set on the time for persistent connections to live (*__60 seconds__*) and for the time for idle connections to stay alive (*__30 seconds__*) before being evicted from the connection pool.

These 2 attributes are configurable and can be set via properties to suit users needs.

In order to change the connection time to live value of a thread, the following property should be set/specified `connection.time.to.live=<value_in_seconds>`.

To change the time for idle threads to stay alive, the following property should be set/specified `idle.connection.timeout=<value_in_seconds>`.

**NOTE:** Property values can be specified: in a properties file, set via the command line arguments using the -D option or set via code. e.g. `System.setProperty(<property_value>)`