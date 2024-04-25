## Writeup for Lab 2

姓名：陈志聪       学号：521120910256        

#### Decision Made for Lab 2

* Describe any design decisions you made, including your choice of page eviction policy. Describe briefly your insertion and deletion methods in B+ tree. Describe your idea in solving the bonus exercise (if it applies).
* I applied LRU page eviction policy in `BufferPool` and exploited bi-direction `linkednode` to implement it.   When a new page non-existent in the buffer comes, the program judges whether the buffer is full, if so,  tail of the linked list will be remove and the coming one will be added to the head.
*  **Insertion Method:** When inserting a new page to the B+ tree, it's possible that the internal or leaf node split into two part to satisfy property of B+ tree. The core methods with regard to insertion are `splitInternalPage() ` and `splitLeafPage()`. 
  * `splitLeafPage()`: I first set a new page as right child of the split page and connect it  to its right sibling(if exists). Then I recursively split the parent and update dirty pages and the pointer.
  * `splitInternalPage():` Firstly, move some of the entries from the left sibling to the page so that the entries are evenly distributed. Then, update the corresponding parent entry. Finally, update the parent pointers of all children in the entries that were moved.
* **Deletion Method:**
  * `stealFromLeafPage():` Firstly, identify steal from right sibling or left sibling. Then determine the number of stolen tuples according to the number of that in the sibling node. At last, update the entry of parent node.
  * `stealFromLeftInternalPage()`、 `stealFromLeftInternalPage()`: Firstly, determine the number of stolen tuples. Then stole from the parent node and left/right node. At last, update parent node and dirty pages.
  * `mergeLeafPages():` Firstly, add all nodes of right  to the left node. Then update the right node and set it empty. Finally, delete entries of parent node and update dirty pages.



#### Changes made to the API

I have not made any changes to the API.



#### Missing or incomplete elements of my code

* Exception capture and process: I may ignore some exceptions of invalid input during my implementation though the code passed all the tests.
* Redundancy: Since the structure of the database is complexity, I may repeat redundant calls which costs much of time.



#### Difficulty

I have spent about one week for this project and encountered lots of problems during debug. The most impressive one is that I've failed hundreds of times on `BTreeFileDeleteTest` system test and the errors existed on casting class `BTreeFile` to class `HeapFile`. After trials and trails, finally, I modified `insertTuple()` and `deleteTuple()` in `BufferPool.java` and set the type of defined variable `DbFile` but `HeapFile`, which resulted in successful building.



#### Test Results

I passed all the unit tests and system test of lab 2.

* Unit Test

![p1](C:\Users\20286\Desktop\p1.png)

![p2](C:\Users\20286\Desktop\p2.png)

* System Test![p3](C:\Users\20286\Pictures\Screenshots\p3.png)

![p4](C:\Users\20286\Pictures\Screenshots\p4.png)