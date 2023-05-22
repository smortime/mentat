# Mentat
Service optimized for logging numbers with summary statistics every 10 seconds. The name comes from [Dune](https://en.wikipedia.org/wiki/Organizations_of_the_Dune_universe#Mentats); basically humans trained to carry out computations that were done by computers.


## Running Mentat
Things were done a bit dirty since I shockingly don't write a lot of Java on my desktop.
```
mvn package -Dmaven.test.skip &&
java -cp target/mentat-1.0-SNAPSHOT.jar org.smort.mentat.MentatServer
```
We are skipping tests because they can be finicky due to my `TestClient`...


**Note:** This project was written and tested with the following
```
➜  mentat lsb_release -a | rg Description
No LSB modules are available.
Description:    Ubuntu 22.04.1 LTS
➜  mentat java --version
openjdk 17.0.6 2023-01-17
OpenJDK Runtime Environment (build 17.0.6+10-Ubuntu-0ubuntu122.04)
OpenJDK 64-Bit Server VM (build 17.0.6+10-Ubuntu-0ubuntu122.04, mixed mode, sharing)
```


In `scripts` there are a couple hacky scripts I used to test throughput:
- **generate_test_input.py**: Script to write text files with correctly formatted input
- **e2e_test.sh**: Uses `nc` to send the test file contents to Mentat


## Assumptions
1. There isn't a hard SLA on when content is written to `numbers.log` (the sooner the better ofc) thus buffering writes to disc in favor of increased throughput was a tradeoff I made
2. We aren't worried about extensibility so many classes are marked final/package private/didn't create nifty interfaces
3. We all agree that with more time I would actually write good Unit/Integration Tests...


## Design Choices
1. Used `BufferedWriter` and didn't `flush` after processing each input; `flush` is expensive and doing so after each input decreased my 10 second throughput greatly;
    - **Note:** Aside from delayed writes in general a limitation of this could lead to major delays for a few inputs until Mentat is shutdown. Could tune buffer size from default to help minimize this.
2. Only one `consumer` due to most of what it is doing would need be synchronized if had multiple; writing to file and checking `BitSet` if we wanted to offload more work to `Reporter` it could be worth refactoring
3. To help with clean server termination used `ConcurrentHashmap` for book keeping of active `sockets` in order to close them on exit to let the `RequestHandlers` stop blocking, could have done just a `Queue` that `Server` is aware of but then we could blow up memory with someone opening closing connections, where the map lets `Clients` cleanup before exit on client closing
4. Didn't bother setting heap size (`-Xmx`) due to set number of threads, if we wanted to enable > 5 clients or keep unique in memory we'd probably fidget with this

