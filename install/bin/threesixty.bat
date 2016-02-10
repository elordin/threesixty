@REM threesixty launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM THREESIXTY_config.txt found in the THREESIXTY_HOME.
@setlocal enabledelayedexpansion

@echo off

if "%THREESIXTY_HOME%"=="" set "THREESIXTY_HOME=%~dp0\\.."

set "APP_LIB_DIR=%THREESIXTY_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%THREESIXTY_HOME%\THREESIXTY_config.txt"
set CFG_OPTS=
if exist %CFG_FILE% (
  FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%CFG_FILE%") DO (
    set DO_NOT_REUSE_ME=%%i
    rem ZOMG (Part #2) WE use !! here to delay the expansion of
    rem CFG_OPTS, otherwise it remains "" for this loop.
    set CFG_OPTS=!CFG_OPTS! !DO_NOT_REUSE_ME!
  )
)

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running threesixty.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)


rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "!_JAVA_OPTS!"=="" set _JAVA_OPTS=!CFG_OPTS!

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=
set _APP_ARGS=

:param_loop
call set _PARAM1=%%1
set "_TEST_PARAM=%~1"

if ["!_PARAM1!"]==[""] goto param_afterloop


rem ignore arguments that do not start with '-'
if "%_TEST_PARAM:~0,1%"=="-" goto param_java_check
set _APP_ARGS=!_APP_ARGS! !_PARAM1!
shift
goto param_loop

:param_java_check
if "!_TEST_PARAM:~0,2!"=="-J" (
  rem strip -J prefix
  set _JAVA_PARAMS=!_JAVA_PARAMS! !_TEST_PARAM:~2!
  shift
  goto param_loop
)

if "!_TEST_PARAM:~0,2!"=="-D" (
  rem test if this was double-quoted property "-Dprop=42"
  for /F "delims== tokens=1,*" %%G in ("!_TEST_PARAM!") DO (
    if not ["%%H"] == [""] (
      set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
    ) else if [%2] neq [] (
      rem it was a normal property: -Dprop=42 or -Drop="42"
      call set _PARAM1=%%1=%%2
      set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
      shift
    )
  )
) else (
  if "!_TEST_PARAM!"=="-main" (
    call set CUSTOM_MAIN_CLASS=%%2
    shift
  ) else (
    set _APP_ARGS=!_APP_ARGS! !_PARAM1!
  )
)
shift
goto param_loop
:param_afterloop

set _JAVA_OPTS=!_JAVA_OPTS! !_JAVA_PARAMS!
:run
 
set "APP_CLASSPATH=%APP_LIB_DIR%\default.threesixty-1.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.11.7.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-xml_2.11-1.0.3.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-actor_2.11-2.4.1.jar;%APP_LIB_DIR%\com.typesafe.config-1.3.0.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-testkit_2.11-2.4.1.jar;%APP_LIB_DIR%\org.skife.com.typesafe.config.typesafe-config-0.3.0.jar;%APP_LIB_DIR%\io.spray.spray-can_2.11-1.3.3.jar;%APP_LIB_DIR%\io.spray.spray-io_2.11-1.3.3.jar;%APP_LIB_DIR%\io.spray.spray-util_2.11-1.3.3.jar;%APP_LIB_DIR%\io.spray.spray-http_2.11-1.3.3.jar;%APP_LIB_DIR%\org.parboiled.parboiled-scala_2.11-1.1.7.jar;%APP_LIB_DIR%\org.parboiled.parboiled-core-1.1.7.jar;%APP_LIB_DIR%\io.spray.spray-caching_2.11-1.3.3.jar;%APP_LIB_DIR%\com.googlecode.concurrentlinkedhashmap.concurrentlinkedhashmap-lru-1.4.2.jar;%APP_LIB_DIR%\io.spray.spray-json_2.11-1.3.2.jar;%APP_LIB_DIR%\com.websudos.phantom-dsl_2.11-1.18.1.jar;%APP_LIB_DIR%\com.websudos.phantom-connectors_2.11-1.18.1.jar;%APP_LIB_DIR%\ch.qos.logback.logback-classic-1.1.3.jar;%APP_LIB_DIR%\ch.qos.logback.logback-core-1.1.3.jar;%APP_LIB_DIR%\org.slf4j.log4j-over-slf4j-1.7.12.jar;%APP_LIB_DIR%\org.slf4j.slf4j-api-1.7.12.jar;%APP_LIB_DIR%\com.datastax.cassandra.cassandra-driver-core-3.0.0-alpha4.jar;%APP_LIB_DIR%\io.netty.netty-handler-4.0.27.Final.jar;%APP_LIB_DIR%\io.netty.netty-buffer-4.0.27.Final.jar;%APP_LIB_DIR%\io.netty.netty-common-4.0.27.Final.jar;%APP_LIB_DIR%\io.netty.netty-transport-4.0.27.Final.jar;%APP_LIB_DIR%\io.netty.netty-codec-4.0.27.Final.jar;%APP_LIB_DIR%\com.google.guava.guava-16.0.1.jar;%APP_LIB_DIR%\com.codahale.metrics.metrics-core-3.0.2.jar;%APP_LIB_DIR%\org.scala-lang.scala-reflect-2.11.7.jar;%APP_LIB_DIR%\com.websudos.diesel-engine_2.11-0.2.2.jar;%APP_LIB_DIR%\com.chuusai.shapeless_2.11-2.2.4.jar;%APP_LIB_DIR%\com.twitter.util-core_2.11-6.24.0.jar;%APP_LIB_DIR%\com.twitter.util-function_2.11-6.24.0.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-parser-combinators_2.11-1.0.2.jar;%APP_LIB_DIR%\com.typesafe.play.play-iteratees_2.11-2.4.0-M1.jar;%APP_LIB_DIR%\org.scala-stm.scala-stm_2.11-0.7.jar;%APP_LIB_DIR%\org.joda.joda-convert-1.6.jar;%APP_LIB_DIR%\com.websudos.phantom-testkit_2.11-1.18.1.jar;%APP_LIB_DIR%\com.websudos.util-lift_2.11-0.10.0.jar;%APP_LIB_DIR%\com.websudos.util-parsers_2.11-0.10.0.jar;%APP_LIB_DIR%\com.websudos.util-domain_2.11-0.10.0.jar;%APP_LIB_DIR%\com.websudos.util-http_2.11-0.10.0.jar;%APP_LIB_DIR%\com.twitter.finagle-http_2.11-6.25.0.jar;%APP_LIB_DIR%\com.twitter.finagle-core_2.11-6.25.0.jar;%APP_LIB_DIR%\io.netty.netty-3.10.1.Final.jar;%APP_LIB_DIR%\com.twitter.util-app_2.11-6.24.0.jar;%APP_LIB_DIR%\com.twitter.util-registry_2.11-6.24.0.jar;%APP_LIB_DIR%\com.twitter.util-collection_2.11-6.24.0.jar;%APP_LIB_DIR%\com.google.code.findbugs.jsr305-1.3.9.jar;%APP_LIB_DIR%\javax.inject.javax.inject-1.jar;%APP_LIB_DIR%\commons-collections.commons-collections-3.2.1.jar;%APP_LIB_DIR%\com.twitter.util-hashing_2.11-6.24.0.jar;%APP_LIB_DIR%\com.twitter.util-stats_2.11-6.24.0.jar;%APP_LIB_DIR%\com.twitter.util-jvm_2.11-6.24.0.jar;%APP_LIB_DIR%\com.twitter.util-logging_2.11-6.24.0.jar;%APP_LIB_DIR%\com.twitter.jsr166e-1.0.0.jar;%APP_LIB_DIR%\com.twitter.util-codec_2.11-6.24.0.jar;%APP_LIB_DIR%\commons-codec.commons-codec-1.6.jar;%APP_LIB_DIR%\commons-lang.commons-lang-2.6.jar;%APP_LIB_DIR%\commons-validator.commons-validator-1.4.0.jar;%APP_LIB_DIR%\commons-beanutils.commons-beanutils-1.8.3.jar;%APP_LIB_DIR%\commons-logging.commons-logging-1.1.1.jar;%APP_LIB_DIR%\commons-digester.commons-digester-1.8.jar;%APP_LIB_DIR%\org.scalaz.scalaz-core_2.11-7.1.0.jar;%APP_LIB_DIR%\net.liftweb.lift-webkit_2.11-3.0-M6.jar;%APP_LIB_DIR%\net.liftweb.lift-util_2.11-3.0-M6.jar;%APP_LIB_DIR%\org.scala-lang.scala-compiler-2.11.4.jar;%APP_LIB_DIR%\net.liftweb.lift-actor_2.11-3.0-M6.jar;%APP_LIB_DIR%\net.liftweb.lift-common_2.11-3.0-M6.jar;%APP_LIB_DIR%\net.liftweb.lift-json_2.11-3.0-M6.jar;%APP_LIB_DIR%\org.scala-lang.scalap-2.11.4.jar;%APP_LIB_DIR%\com.thoughtworks.paranamer.paranamer-2.4.1.jar;%APP_LIB_DIR%\net.liftweb.lift-markdown_2.11-3.0-M6.jar;%APP_LIB_DIR%\joda-time.joda-time-2.6.jar;%APP_LIB_DIR%\javax.mail.mail-1.4.4.jar;%APP_LIB_DIR%\javax.activation.activation-1.1.jar;%APP_LIB_DIR%\nu.validator.htmlparser.htmlparser-1.4.jar;%APP_LIB_DIR%\xerces.xercesImpl-2.11.0.jar;%APP_LIB_DIR%\xml-apis.xml-apis-1.4.01.jar;%APP_LIB_DIR%\commons-fileupload.commons-fileupload-1.2.2.jar;%APP_LIB_DIR%\org.mozilla.rhino-1.7R4.jar;%APP_LIB_DIR%\com.websudos.util-testing_2.11-0.10.0.jar;%APP_LIB_DIR%\org.scalatest.scalatest_2.11-2.2.4.jar;%APP_LIB_DIR%\org.scalacheck.scalacheck_2.11-1.11.4.jar;%APP_LIB_DIR%\org.scala-sbt.test-interface-1.0.jar;%APP_LIB_DIR%\org.fluttercode.datafactory.datafactory-0.8.jar;%APP_LIB_DIR%\io.spray.spray-routing-shapeless2_2.11-1.3.3.jar;%APP_LIB_DIR%\io.spray.spray-httpx_2.11-1.3.3.jar;%APP_LIB_DIR%\org.jvnet.mimepull.mimepull-1.9.5.jar;%APP_LIB_DIR%\com.typesafe.play.play-json_2.11-2.3.4.jar;%APP_LIB_DIR%\com.typesafe.play.play-functional_2.11-2.3.4.jar;%APP_LIB_DIR%\com.typesafe.play.play-datacommons_2.11-2.3.4.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-annotations-2.3.2.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-core-2.3.2.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-databind-2.3.2.jar"
set "APP_MAIN_CLASS=threesixty.server.Server"

if defined CUSTOM_MAIN_CLASS (
    set MAIN_CLASS=!CUSTOM_MAIN_CLASS!
) else (
    set MAIN_CLASS=!APP_MAIN_CLASS!
)

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" !_JAVA_OPTS! !THREESIXTY_OPTS! -cp "%APP_CLASSPATH%" %MAIN_CLASS% !_APP_ARGS!

@endlocal


:end

exit /B %ERRORLEVEL%
