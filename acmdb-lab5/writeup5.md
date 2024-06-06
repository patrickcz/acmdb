## Writeup for Lab 5

姓名：陈志聪       学号：521120910256        

#### Decision Made for Lab 5

* Deadlock detection policy: When a potential deadlock is detected, the system analyzes the transactional dependencies to identify the deadlock. Upon detection, the system resolves the deadlock by aborting one or more transactions involved. Deadlock events are logged for auditing and analysis purposes. Administrators may intervene to adjust system settings or optimize queries to mitigate future deadlocks.

* Locking granularity: I applied a mixed or hybrid strategy. This strategy dynamically adjusts the level of locking based on the accessed data and concurrency requirements. For frequently accessed and heavily contended resources, fine-grained locking can be utilized to maximize concurrency and reduce contention. Conversely, for less frequently accessed or larger resources, coarse-grained locking can be applied to minimize overhead and simplify management. The system continuously evaluates access patterns and contention levels to adapt the locking granularity accordingly, ensuring an optimal balance between concurrency and performance. This adaptive approach offers flexibility and efficiency in managing concurrent transactions within the database system.


#### Changes made to the API

I have not made any changes to the API.



#### Missing or incomplete elements of my code

I have not found missing or incomplete elements of my code ignoring the bonus part. 



#### Difficulty

I have spent about two days on lab5, and I think the most challenge part is to deal with the lock



#### Test Results

I passed all the unit tests and system test of lab 5.

* Unit Test

![p1](C:\Users\20286\Desktop\p1.png)

![p2](C:\Users\20286\Desktop\p2.png)

* System Test

![p4](C:\Users\20286\Desktop\p4.png)

![p5](C:\Users\20286\Desktop\p5.png)