## Writeup for Lab 4

姓名：陈志聪       学号：521120910256        

#### Decision Made for Lab 4

* Methods for selectivity estimation：` estimateSelectivity()` in `IntHistogram.java`.  The steps are listed as follows:

  * For the equality operation `value = const`, we first find the bucket containing the const value, and then perform the calculation:  $selectivity = \#records(value = const) / \#records$. Assuming the values are uniformly distributed within the bucket, the number of records where value equals to const is equal to the bucket width divided by the bucket height. Therefore, selectivity can be represented as (bucket height / bucket width) divided by the total number of records.
  * For non-equality operations, we adopt the same approach. The selectivity for value > const is calculated as (number of records where value > const) / total number of records. The number of records where value > const is composed of two parts in the histogram: the number of records in the interval $(const, b_{right}]$ and the number of records in the interval $[b_{right}, max]$. The number of records in the interval$(const, b_{right}]$ is calculated as $(h_b / w_b) * (b_{right} - const)$, and the number of records in the interval [b.right, max] is the sum of the heights of the subsequent buckets.

* Join Ordering：

  ```
  produe FindBestPlan(S)
  	if(bestplan[S].cost ≠ ∞ ) 	//bestplan[S]已经计算好了
  		return bestplan[S]
  	if(S中只包含一个关系)
  		根据访问S的最佳方式设置bestplan[S].plan和bestplab[S].cost
  	else
  		for each S 的非空子集S1，且S1≠S
  			P1 = FindBestPlan(S1)
  			P2 = FindBestPlan(S-S1)
  			A = 连接P1和P2的结果的算法   //嵌套循环连接
  			plan = 使用A对P1和P2进行连接的结果
  			cost = P1.cost + P2.cost + A的代价
  			if  cost < bestplan[S].cost
  				bestplan[S].cost = cost
  				bestplan[S].plan = plan
  return bestplan[S]
  ```

  

#### Changes made to the API

I have not made any changes to the API.



#### Missing or incomplete elements of my code

I have not found missing or incomplete elements of my code ignoring the bonus part. 



#### Difficulty

I have spent about one day on lab4, and fortunately I have not met extremely challenging part in the lab.



#### Test Results

I passed all the unit tests and system test of lab 4.

* Unit Test

![p1](C:\Users\20286\Desktop\p1.png)

![p2](C:\Users\20286\Desktop\p2.png)

* System Test

![p3](C:\Users\20286\Desktop\p3.png)

![p4](C:\Users\20286\Desktop\p4.png)