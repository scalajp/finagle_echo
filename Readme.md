# finagle\_echo - an echo client/server using finagle

This project provides a simple example finagle system using JSON as messaging.
Using this, clients can send json messages to the server and retrieve echo back
from server easily.

## Usage

1. Start server

At the first, type the following command in a console.

    $./sbt run

Then, the following message should be displayed:

    [1] org.scala_users.jp.finagle_echo.MessagingClient
    [2] org.scala_users.jp.finagle_echo.MessagingServer

    Enter number:

Press 2 and [Enter].

2. Start client

Type the following command in another console.

    $./sbt run

Then, the following message should be displayed:

    [1] org.scala_users.jp.finagle_echo.MessagingClient
    [2] org.scala_users.jp.finagle_echo.MessagingServer

    Enter number:

Press 1 and [Enter].

3. Confirm result

If the programs has been executed correctly, the following result should be displayed in the
client side console:

    JString(Hello, World)
    [success] Total time: 4 s, completed 2012/04/30 14:25:41
