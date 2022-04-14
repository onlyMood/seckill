package com.mz;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.List;


public class SecKill_redis {

    public static  boolean doSecKill(String userid, String prodid) throws IOException{
        if( userid == null && prodid == null ){
            return false;
        }
        //连接redis
        //Jedis jedis=new Jedis("192.168.91.130",6379);
        JedisPool jedisPoolInstance = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedisPoolInstance.getResource();
        //拼接key
        String countKey="seckill:"+prodid+":count";
        String userKey="seckill:"+prodid+":user";
        //监视库存数量
        jedis.watch(countKey);
        //判断库存
        String count=jedis.get(countKey);
        if(count==null){
            System.out.println("秒杀还没开始");
            jedis.close();
            return false;
        }
        //判断用户，一个人只能秒杀一次
        if(jedis.sismember(userKey,userKey)){
            System.out.println("已经秒杀成功了，不能重复秒杀");
            jedis.close();
            return false;
        }
        //判断库存数量，小于1秒杀结束
        if(Integer.parseInt(count)<=0){
            System.out.println("秒杀结束");
            jedis.close();
            return false;
        }
        //秒杀过程
        //添加事务
        Transaction multi = jedis.multi();
        multi.decr(countKey);
        multi.sadd(userKey,userid);
        List<Object> results = multi.exec();
        if (results == null || results.size() == 0) {
            System.out.println("秒杀失败");
            jedis.close();
            return false;
        }
        //jedis.decr(countKey);
        //jedis.sadd(userKey,userid);

        System.out.println("秒杀成功");
        jedis.close();
        return true;
    }

}
