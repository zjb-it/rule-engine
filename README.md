# 规则引擎

开发规则平台的意义：
>1. 低代码的，可视化的规则配置平台  
>2. 快速集成:在依赖其它系统接口的时候，不再需要额外工作量，只需要注册接口，即可完成集成  
>3. 在保证运行结果正确的情况下，优化数据结构，减少内存开销，提升运行速度  

*测试链接：数据每天24点会重置* https://www.pincheche.online/rule-front/

**[规则引擎文档](https://github.com/zjb-it/rule-engine/blob/master/README.md)**

**[规则平台前端文档](https://github.com/zjb-it/rule-platform-front/blob/master/README.md)**

**[更多开源软件 https://www.pincheche.online/](https://www.pincheche.online/)**

## 概念
### 基础概念
1. 固定值：包括NUMBER, STRING, COLLECTION,
    BOOLEAN, JSONOBJECT，用户可以直接输入格式相对应的常量
2. 元素：即参数，用户调用规则需要传入的参数（不是必须的），后面会详细解释
3. 变量：  
a. 主要功能为绑定函数进行计算，绑定的函数赋予了变量的功能；  
b. 拥有参数，在计算过程中参数会转换为绑定的函数所需要的参数  
c. 参数可以由变量，元素，固定值，（用户也可以继承Value类进行扩展）等组成  
4. 函数：包括用户自己开发的内部函数；也可以通过注册接口成为函数，接口将通过http的请求进行调用
5. 条件：由左值 ，运算符号，右值组成，左值和右值可以是固定值，元素，变量
6. 条件组：条件的集合，条件之间为&&关系
7. 条件集：条件组的集合，条件组之间为 || 关系
8. 规则：由条件和动作组成，条件计算结果为true,则返回动作的运算结果  
 	 a. 条件可以是单个条件，条件组，条件集等  
 	 b. 动作可以是固定值，元素，变量等  
9. 规则集：由多个规则组成，根据策略可以选择，  
    a. 按顺序运行规则，触发一个规则即返回  
 	b. 按顺序运行所有规则，返回所有触发规则 的执行结果的集合  
 	c. 随机运行所有规则，触发一个规则即返回  
10.  决策树：主要用来解决具有大量重复条件的规则集，会构建为一棵决策树，达到节省内存，提升运行速度，可以用表格进行编辑，支持让用户一键导入， 


## 特点

 >1. 节省内存:通过树的数据结果 和享元模式的设计，达到节省内存的目的
 >2. 提升运行速度:根据权重优化执行顺序，达到fast-fail，从而提升速度
 >3. 快速接入其它系统 ：可以直接注册接口封装为函数，0成本接入其它系统，不需要迁入其它系统的代码 

## 设计
![设计](https://github.com/zjb-it/rule-engine/blob/master/screenshot/design.jpg)

规则引擎主要包含3种类型的规则及一个函数容器

### 支持的计算符号

<table>
<thead>
   <tr>
      <th>左值类型</th>
      <th>运算符</th>
      <th>中文解释</th>
      <th>右值类型</th>
   </tr>
   </thead>
   <tbody>
   <tr>
      <td rowspan="9">String 字符串</td>
      <td>=</td>
      <td>等于</td>
      <td rowspan="8">String 字符串</td>
   </tr>
   <tr>      
      <td>!=</td>
      <td>不等于</td>      
   </tr>
   <tr>      
      <td>indexOf</td>
      <td>A包含子串B</td>      
   </tr>
   <tr>      
      <td>notIndexOf</td>
      <td>A不包含子串B</td>      
   </tr>
   <tr>      
      <td>startWith</td>
      <td>A以B开头</td>      
   </tr>
   <tr>      
      <td>notStartWith</td>
      <td>A不以B开头</td>      
   </tr>
   <tr>      
      <td>endWith</td>
      <td>A以B结尾</td>      
   </tr>
   <tr>      
      <td>notEndWith</td>
      <td>A不以B结尾</td>      
   </tr>
   <tr>      
      <td>in</td>
      <td>A串在B集合中</td>
      <td>集合</td>
   </tr>
   <tr>
      <td rowspan="7">Number 数值类型</td>
      <td>=</td>
      <td>等于</td>
      <td rowspan="6">Number 数值类型</td>
   </tr>
   <tr>      
      <td>!=</td>
      <td>不等于</td>      
   </tr>
   <tr>      
      <td>></td>
      <td>大于</td>      
   </tr>
   <tr>      
      <td>>=</td>
      <td>大于等于</td>      
   </tr>
   <tr>      
      <td><</td>
      <td>小于</td>      
   </tr>
   <tr>      
      <td><=</td>
      <td>小于等于</td>      
   </tr>
   <tr>      
      <td>in</td>
      <td>A串在B集合中</td>
      <td>集合</td>
   </tr>
   <tr>
      <td rowspan="3">Boolean 布尔类型</td>
      <td>=</td>
      <td>等于</td>
      <td rowspan="2">Boolean 布尔类型</td>
   </tr>
   <tr>      
      <td>!=</td>
      <td>不等于</td>      
   </tr>
   <tr>      
      <td>in</td>
      <td>A串在B集合中</td>
      <td>集合</td>
   </tr>
   <tr>
      <td rowspan="10">Collection 集合</td>
      <td rowspan="4">contain</td>
      <td rowspan="4">A集合包含B</td>
      <td>字符串</td>
   </tr>
   <tr>
      <td>数值</td>
   </tr>
   <tr>
      <td>布尔</td>
   </tr>
   <tr>
      <td>集合</td>
   </tr>
   <tr>      
      <td rowspan="4">notContain</td>
      <td rowspan="4">A集合不包含B</td>
      <td>字符串</td>
   </tr>
   <tr>
      <td>数值</td>
   </tr>
   <tr>    
      <td>布尔</td>
   </tr>
   <tr>    
       <td>集合</td>
   </tr>
   <tr>      
      <td> =</td>
      <td>A集合等于B集合</td>
      <td>集合</td>
   </tr>
   <tr>      
      <td>in</td>
      <td>A集合是B集合的子集</td>
      <td>集合</td>
   </tr>   
   </tbody>
</table>

### 规则：
>主要包含条件及结果，条件可以是单个条件，条件组，或条件集，加载规则时，在不改变执行结果的情况下，默认会根据条件的权重进行优化排序，以提高执行效率，条件==true, 则返回结果

>####执行过程

![规则执行过程](https://github.com/zjb-it/rule-engine/blob/master/screenshot/runRule.jpg)
 
### 规则集：
>规则的集合，加载规则集时，在不改变执行结果的情况下，默认会根据规则的权重进行优化排序，以提高执行效率，根据选择的执行策略，返回相对应的结果

![规则集执行过程](https://github.com/zjb-it/rule-engine/blob/master/screenshot/runRuleSet.jpg)

### 决策表
>可以理解为一种特别的规则集，其中的规则都拥有相似的条件，加载决策表时，会构建决策树以节省内存，运行期间根据策略运行不同的算法，以提高执行效率
>#### 建树过程：

![建树过程](https://github.com/zjb-it/rule-engine/blob/master/screenshot/buildtree.jpg)

>>1.逐行处理每一条规则，因为每条规则包含的条件列、结果列数量是一定的。因此当第一行数据插入后，树的层数已经确定。  
>>2.每插入一条新的规则，匹配已有节点：  
>>>a.没有匹配则新增节点  
>>>b.当到达叶子节点（结果节点），将结果作为叶子节点的属性。若有节点，将结果合并，反之新增。  
>>>c.对于空条件，创建“恒成立“节点  
>>>	d.对于区间类条件，建树成本较高，例如条件1：值 > x，则需要遍历同一层所有>x的值节点，并且在多个条件节点后加入相同。(注:由于将成本开销控制在引擎建树阶段，依然可以保持引擎运行态的高效性。)  
>#### 执行过程：

![决策表执行过程](https://github.com/zjb-it/rule-engine/blob/master/screenshot/runDecision.jpg)

>>
>>对于决策树而言，存在单一匹配和完全匹配两种策略, 有着不同的执行过程。以单一匹配策略为例, 运行过程如下所示:
>>单一匹配策略：  
>>>a. 从决策树的根节点进入，顺序遍历所有子节点，如果任意一节点的条件为true，则进行如下判断:  
>>>b. 该节点是否是具有结果属性的叶子节点？如果是，返回叶子节点的结果集中的第一个结果。  
>>>c. 如果该节点是叶子节点，但不具有结果属性，则返回空。  
>>>d. 如果该节点不是叶子节点，递归向下进行遍历，寻找具有结果属性的叶子节点，直到找到最后一层的节点为止。  
>>>如果所有条件都不成立，则返回空。
>
>>全匹配策略：
>>>执行过程与单一匹配类似，在 b 过程中，使用回溯+备忘录获取所有结果
 

## 样例

[详见测试用例](https://github.com/zjb-it/rule-engine/tree/master/src/test/java/com/zjb/ruleengine)


