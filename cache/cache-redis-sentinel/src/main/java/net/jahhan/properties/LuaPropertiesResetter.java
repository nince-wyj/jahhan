package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;
import net.jahhan.common.extension.api.PropertiesResetter;

@PropertiesFile("lua")
@Order(1)
public class LuaPropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		properties.setProperty("releaseNoneReentrantLock",
				"if redis.call('GET',KEYS[1])==KEYS[2] then return redis.call('del',KEYS[1]) else return 0 end");
		properties.setProperty("releaseGlobalReentrantLock",
				"if redis.call('GET','LEVEL:'..KEYS[1])==KEYS[3] then local tmp=redis.call('GET',KEYS[1]) if tmp==KEYS[2] then local level=redis.call('DECR','LEVEL:'..KEYS[1]) if level==0 then redis.call('DEL','LEVEL:'..KEYS[1]) redis.call('DEL',KEYS[1]) redis.call('SREM','CHAIN_LOCKS:'..KEYS[2],KEYS[1]) return 0 else if level>0 then return level else return -1 end end else return -1 end else return -1 end");
		properties.setProperty("getGlobalReentrantLock",
				"local level=redis.call('GET','LEVEL:'..KEYS[1]) if level==false and tonumber(KEYS[3])==0 or level==KEYS[3] then local lock=redis.call('SET',KEYS[1],KEYS[2],'NX','EX',KEYS[4]) if lock then redis.call('SETEX','LEVEL:'..KEYS[1],KEYS[4],'1') redis.call('SADD','CHAIN_LOCKS:'..KEYS[2],KEYS[1]) redis.call('EXPIRE','CHAIN_LOCKS:'..KEYS[2],KEYS[4]) return 1 else if KEYS[2]==redis.call('GET',KEYS[1]) then return redis.call('INCR','LEVEL:'..KEYS[1]) else redis.call('LPUSH','QUEUE:'..KEYS[1],KEYS[2]) return 0 end end else return -1 end");
		properties.setProperty("releaseChainLock",
				"local members=redis.call('SMEMBERS','CHAIN_LOCKS:'..KEYS[1]) for i=1,#members do redis.call('DEL', members[i])  redis.call('DEL', 'LEVEL:'..members[i]) redis.call('DEL', 'LOCK_TIMEOUT:'..members[i]) redis.call('DEL','LOCK_TIMEOUT_NOTIFY:'..members[i]..'|'..KEYS[1]) local keep=redis.call('LRANGE','QUEUE:'..members[i],0,1) if keep[1]==KEYS[1] and keep[2] then redis.call('PUBLISH','__keyevent@0__:WEAKUP','WEAKUP_NOTIFY:'..members[i]..'|'..keep[2]) else  end redis.call('LREM','QUEUE:'..members[i],0,KEYS[1]) end redis.call('DEL', 'CHAIN_LOCKS:'..KEYS[1])");

		properties.setProperty("queueGetGlobalReentrantLock",
				"local keep=redis.call('LRANGE','QUEUE:'..KEYS[1],0,0) if keep and keep[1]==KEYS[2] then if KEYS[3]==redis.call('GET','LEVEL:'..KEYS[1]) then return redis.call('INCR','LEVEL:'..KEYS[1]) else return -1 end else local len=redis.call('LLEN','QUEUE:'..KEYS[1]) if len==0 then redis.call('RPUSH','QUEUE:'..KEYS[1],KEYS[2]) redis.call('SETEX','LEVEL:'..KEYS[1],KEYS[4],'1') redis.call('SETEX','LOCK_TIMEOUT:'..KEYS[1],KEYS[4],KEYS[2]) redis.call('SETEX','LOCK_TIMEOUT_NOTIFY:'..KEYS[1]..'|'..KEYS[2],KEYS[4],'') redis.call('SADD','CHAIN_LOCKS:'..KEYS[2],KEYS[1]) redis.call('EXPIRE','CHAIN_LOCKS:'..KEYS[2],KEYS[4]) return 1 else local timeout=redis.call('EXISTS','LOCK_TIMEOUT:'..KEYS[1]) redis.call('RPUSH','QUEUE:'..KEYS[1],KEYS[2]) if timeout>0 then return 0 else redis.call('LPOP','QUEUE:'..KEYS[1]) redis.call('SETEX','LEVEL:'..KEYS[1],KEYS[4],'1') redis.call('SETEX','LOCK_TIMEOUT:'..KEYS[1],KEYS[4],KEYS[2]) redis.call('SETEX','LOCK_TIMEOUT_NOTIFY:'..KEYS[1]..'|'..KEYS[2],KEYS[4],'') redis.call('SADD','CHAIN_LOCKS:'..KEYS[2],KEYS[1]) redis.call('EXPIRE','CHAIN_LOCKS:'..KEYS[2],KEYS[4]) return 1 end end end return -10");
		properties.setProperty("unLockGlobalReentrantLock",
				"local keep=redis.call('LRANGE','QUEUE:'..KEYS[1],0,1) if keep and keep[1]==KEYS[2] then if KEYS[3]==redis.call('GET','LEVEL:'..KEYS[1]) then local level=redis.call('DECR','LEVEL:'..KEYS[1]) if level>0 then return level else redis.call('LPOP','QUEUE:'..KEYS[1]) redis.call('DEL','LEVEL:'..KEYS[1]) redis.call('DEL','LOCK_TIMEOUT:'..KEYS[1]) redis.call('DEL','LOCK_TIMEOUT_NOTIFY:'..KEYS[1]..'|'..KEYS[2]) redis.call('SREM','CHAIN_LOCKS:'..KEYS[2],KEYS[1]) if redis.call('LLEN','QUEUE:'..KEYS[1])>0 then redis.call('PUBLISH','__keyevent@0__:WEAKUP','WEAKUP_NOTIFY:'..KEYS[1]..'|'..keep[2]) redis.call('SETEX','WEAKUP_TIMEOUT_NOTIFY:'..KEYS[1]..'|'..keep[2],1,'') end return 0 end else return -1 end else return -2 end return -10");
		properties.setProperty("callGetGlobalReentrantLock",
				"local keep=redis.call('LRANGE','QUEUE:'..KEYS[1],0,0) if keep and keep[1]==KEYS[2] then redis.call('SETEX','LEVEL:'..KEYS[1],KEYS[3],'1') redis.call('SETEX','LOCK_TIMEOUT:'..KEYS[1],KEYS[3],KEYS[2]) redis.call('SETEX','LOCK_TIMEOUT_NOTIFY:'..KEYS[1]..'|'..KEYS[2],KEYS[3],'') redis.call('SADD','CHAIN_LOCKS:'..KEYS[2],KEYS[1]) redis.call('EXPIRE','CHAIN_LOCKS:'..KEYS[2],KEYS[4]) redis.call('DEL','WEAKUP_TIMEOUT_NOTIFY:'..KEYS[1]..'|'..KEYS[2]) return 1 else return -1 end return -10");
		properties.setProperty("competeGetGlobalReentrantLock",
				"local keep=redis.call('LRANGE','QUEUE:'..KEYS[1],0,0) if keep and keep[1]==KEYS[3] then redis.call('LPOP','QUEUE:'..KEYS[1]) redis.call('LREM','QUEUE:'..KEYS[1],0,KEYS[2]) redis.call('LPUSH','QUEUE:'..KEYS[1],KEYS[2]) redis.call('SETEX','LEVEL:'..KEYS[1],KEYS[4],'1') redis.call('SETEX','LOCK_TIMEOUT:'..KEYS[1],KEYS[4],KEYS[2]) redis.call('SETEX','LOCK_TIMEOUT_NOTIFY:'..KEYS[1]..'|'..KEYS[2],KEYS[4],'') redis.call('SADD','CHAIN_LOCKS:'..KEYS[2],KEYS[1]) redis.call('EXPIRE','CHAIN_LOCKS:'..KEYS[2],KEYS[4]) return 1 else return 0 end return -10");
	}
}
