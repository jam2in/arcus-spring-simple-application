# arcus-spring-simple-application
Simple example application using arcus-spring

Collections.synchronizedMap(new HashMap<>()) 자료구조에 저장된 데이터를 인메모리 데이터베이스로 사용하여 캐싱이 잘 되는지를 테스트하는 어플리케이션입니다.
데이터베이스에 초기 데이터를 생성해두고, 아래의 8가지 동작을 각 확률에 따라 Random 객체의 난수를 기반으로 하여 while문으로 반복 수행합니다.

    /* default action ratio
     * 1. DELETE_ARTICLES_BETWEEN : 00.01%
     * 2. DELETE_USER             : 00.02%
     * 3. DELETE_ARTICLE          : 00.07%
     * 4. INSERT_USER             : 01.00%
     * 5. GET_USER                : 05.00%
     * 6. GET_ARTICLES_BETWEEN    : 20.30%
     * 7. INSERT_ARTICLE          : 30.00%
     * 8. GET_ARTICLES            : 43.60%
     */

이를 통해 arcus-spring의 @Cacheable, @CachePut, @CacheEvict 어노테이션이 잘 동작하는지 확인합니다.
데이터베이스에 저장되는 데이터는 Random 객체의 난수 기반으로 생성되는 문자열입니다.
