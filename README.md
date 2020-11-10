Advantages:

- We don't need to have polymorphic objects in memory that represents the single enrichment
and scan-execute all of them, saving values in abstract class

- WeightService and NameService can be tested separately. They have to return the data
or throw an exception

- in ServiceTest, you don't need to verify that you call weightService or nameService.
you could just create tests to verify that eventPublisher and repository are called
with the correct data.
assert-on-data over assert-on-behaviour

- less classes and no hierarchy 

- if an enrichment is optional, it would not create a left when there's an exception

Downsides: 

- Either can handle only one error, see specific test in ServiceTest
    
- you can't assert on the whole EnrichmentResult object. If you add a new enrichment method and use it with flatMap, 
then you'll break the tests written until that moment...

- can you find a way to abstract the test of possible combinations of failures?
    - weight succeeded and name failed
    - weight failed and name succeeded
    - both fails
    - both succeeded
Actually: if you have tests on services and you are sure services throws exception when
they fail, then in ServiceTest you don't need to test all the possible combinations of failures



- Data in EnrichmentResult not isolated, for example addWeight can change data used by addName    
