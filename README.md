# ip-address-counter without GUI
App counts unique ip v4 addresses in src file

![Java](https://img.shields.io/badge/-Java-05122A?style=flat&logo=Java&logoColor=FFA518) ![Large File](https://img.shields.io/badge/-Large_File-05122A?style=flat&logo=none) ![Concurrent](https://img.shields.io/badge/-Concurrent-05122A?style=flat&logo=Concurrent) ![Efficiency](https://img.shields.io/badge/-Efficiency-05122A?style=flat) ![Maven](https://img.shields.io/badge/-Maven-05122A?style=flat&logo=apachemaven&logoColor=fffffb)

The program counts unique IPv4 addresses line by line in the source file.
The format of the addresses is 192.201.12.168 (separated by dots).
It uses a three-dimensional array of BitSets BitSet[][][] with each BitSet object (256) for storage.
For this object, it requires a little over ~1096 MB of RAM (~256x256x256x256 bits).

It was tested on a 64-bit system with Xmx1256m.
Single-threaded mode (sequential: reading a line, then processing it) - tested on a file of about 70GB - several billion addresses.

Multi-threaded mode uses boolean[][][][] (4GB RAM) and has not been tested on a large scale. It requires a little more memory:
- one thread reads the file and puts lines in a queue; if the queue is filled more than the specified value, it waits;
- several other threads take the first line in the queue, process it, and return for the next one.
- this continues until the entire file is read and the queue is empty. Accordingly, all these additional objects require additional RAM.
