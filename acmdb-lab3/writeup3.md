## Writeup for Lab 3

姓名：陈志聪       学号：521120910256        

#### Decision Made for Lab 3

* I applied LRU page eviction policy in `BufferPool` and exploited bi-direction `linkednode` to implement it.   When a new page non-existent in the buffer comes, the program judges whether the buffer is full, if so,  tail of the linked list will be remove and the coming one will be added to the head.
* `AggHandler`: To implement `IntegerAggregator`, I defined a new inner class `AggHandler`to store the result of aggregation of corresponding field.     
* `HeapPage`: New attributes have been added in class `HeapPage`: `tid` and `dirty`(boolean) and therefore some methods have to be modified.



#### Changes made to the API

I have not made any changes to the API.



#### Missing or incomplete elements of my code

* Complexity and Redundancy: I mainly focused on the implementation of each operator but overlooked the efficiency of the program, which caused significant running time.



#### Difficulty

I have spent about two whole days for this lab and it seems easier than lab2. Exactly, I have not encounter any extremely challenging problems and what troubled me a lot is some details mistakes. For instance, I could not pass the `BufferPoolWriteTest` for many times and finally modified some methods in `HeapFile.jave`  and `HeapPage.java` to pass the system test.  What's more, dealing with iterator did cost time and labor.

#### Test Results

I passed all the unit tests and system test of lab 3.

* Unit Test

![p1](C:\Users\20286\Desktop\p1.png)

![p2](C:\Users\20286\Pictures\Screenshots\p2.png)

* System Test

![p3](C:\Users\20286\Pictures\Screenshots\p3.png)

![p4](C:\Users\20286\Pictures\Screenshots\p4.png)