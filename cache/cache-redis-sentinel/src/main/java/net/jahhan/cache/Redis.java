package net.jahhan.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import net.jahhan.cache.constants.RedisConnectType;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.spi.DBCache;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.Pool;
import redis.clients.util.SafeEncoder;

public class Redis implements DBCache {

	public Redis(Pool<Jedis> jedisPool, RedisConnectType redisConnectType) {
		super();
		this.jedisPool = jedisPool;
		this.redisConnectType = redisConnectType;
	}

	private Pool<Jedis> jedisPool;
	private RedisConnectType redisConnectType;

	public JedisTemplate getTemplate() {
		if (RedisConstants.isInUse()) {
			return new JedisTemplate(jedisPool,redisConnectType);
		} else {
			return null;
		}
	}

	public Pool<Jedis> getJedisPool() {
		return jedisPool;
	}

	// TODO//////////////// key（键）///////////////////////////

	/**
	 * 删除一个或多个键
	 * 
	 * @param keys
	 */
	public Long del(final String... keys) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.del(keys);
				}
			});
		}
		return null;
	}

	/**
	 * 判断键是否存在
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Boolean>() {
				public Boolean invoke(Jedis jedis) {
					return jedis.exists(key);
				}
			});
		} else {
			return false;
		}
	}

	/**
	 * 给键设置过期时间
	 * 
	 * @param cachedKey
	 * @param seconds
	 */
	@Override
	public void expire(final String cachedKey, final int seconds) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.expire(cachedKey, seconds);
					return null;
				}
			});
		}
	}

	/**
	 * 获取当前库的所有主键
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> keys(final String match) {
		if (RedisConstants.isInUse()) {
			if (StringUtils.isEmpty(match)) {
				return null;
			}
			return getTemplate().executeRead(new JedisCallBackHandler<Set<String>>() {
				public Set<String> invoke(Jedis jedis) {
					return jedis.keys(match);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 移除过期时间
	 * 
	 * @param cachedKey
	 */
	public void persist(final String cachedKey) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.persist(cachedKey);
					return null;
				}
			});
		}
	}

	/**
	 * 设置过期时间
	 * 
	 * @param cachedKey
	 * @param unixTime
	 * 
	 */
	public Long pexpireAt(final String cachedKey, final long time) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.pexpireAt(cachedKey, time);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 查看过期时间
	 * 
	 * @param key
	 * @return
	 */
	public Long ttl(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.ttl(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回指定数量的匹配值
	 * 
	 * @param cursor
	 * @param count
	 * @param patterns
	 * @return
	 */
	public ScanResult<String> scan(final String cursor, final Integer count, final String... patterns) {
		ScanParams params = new ScanParams();
		for (String pattern : patterns) {
			params.match(pattern);
		}
		if (null != count) {
			params.count(count);
		}
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<ScanResult<String>>() {
				public ScanResult<String> invoke(Jedis jedis) {
					return jedis.scan(cursor, params);
				}
			});
		} else {
			return null;
		}
	}

	// TODO//////////////// string（字符串）///////////////////////////
	/**
	 * 往字符串后面添加字符
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Long append(final String key, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.append(key, value);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 计算给定字符串中，被设置为 1 的比特位的数量
	 * 
	 * @param key
	 * @return
	 */
	public Long bitCount(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.bitcount(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 对一个或多个保存二进制位的字符串 key 进行位元操作，并将结果保存到 destkey 上。 operation 可以是 AND 、 OR 、
	 * NOT 、 XOR 这四种操作中的任意一种
	 * 
	 * @param op
	 * @param destKey
	 * @param srcKeys
	 * @return
	 */
	public Long bitTop(final BitOP op, final String destKey, String... srcKeys) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.bitop(op, destKey, srcKeys);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 功能描述: <br>
	 * 减1
	 */
	public long decr(final String key) {
		if (RedisConstants.isInUse()) {
			Long v = getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.decr(key);
				}
			});
			return v != null ? v.longValue() : -1;
		} else {
			return -1;
		}
	}

	/**
	 * 功能描述: <br>
	 * 减去给定值
	 */
	public long decrBy(final String key, final Integer value) {
		if (RedisConstants.isInUse()) {
			Long v = getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.decrBy(key, value);
				}
			});
			return v != null ? v.longValue() : -1;
		} else {
			return -1;
		}
	}

	private static String sha_decrWhenBig = "994ffa2259f5f6de64a83969fa08f2b00fa771b6";

	/**
	 * 如果健的值大于给定值，则减少
	 * 
	 * @param key
	 * @param compare
	 *            给定值
	 * @param decrValue
	 *            减少的值
	 * @return
	 */
	public Long decrWhenBig(final String key, final String compare, final String decrValue) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return (Long) jedis.evalsha(sha_decrWhenBig, 3, key, compare, decrValue);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获得值
	 * 
	 * @param key
	 * @return
	 */
	public byte[] getBinary(final byte[] key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<byte[]>() {
				public byte[] invoke(Jedis jedis) {
					return jedis.get(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获得值
	 * 
	 * @param key
	 * @return
	 */
	public String get(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.get(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)
	 * 
	 * @param key
	 * @param offset
	 * @return
	 */
	public Boolean getBit(final String key, final long offset) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Boolean>() {
				public Boolean invoke(Jedis jedis) {
					return jedis.getbit(key, offset);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回 key 中字符串值的子字符串，字符串的截取范围由 start 和 end 两个偏移量决定(包括 start 和 end 在内)。
	 * 
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return
	 */
	public String getRange(final String key, final long startOffset, final long endOffset) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.getrange(key, startOffset, endOffset);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 功能描述: <br>
	 * 加1
	 */
	public long incr(final String key) {
		if (RedisConstants.isInUse()) {
			Long v = getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.incr(key);
				}
			});
			return v != null ? v.longValue() : -1;
		} else {
			return -1;
		}
	}

	public long incrBy(final String key, final long addValue) {
		if (RedisConstants.isInUse()) {
			Long v = getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.incrBy(key, addValue);
				}
			});
			return v != null ? v.longValue() : -1;
		} else {
			return -1;
		}
	}

	private static String sha_incrWhenSmall = "d39a90b57136fda66e44a9943e4a2b58822aa7e1";

	/**
	 * 如果值小于对比值，则增加
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Long incrWhenSmall(final String key, final String compare, final String incrValue) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return (Long) jedis.evalsha(sha_incrWhenSmall, 3, key, compare, incrValue);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public String getSet(final String key, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.getSet(key, value);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获取多个key的值，值的顺序跟key一致，如果key对应的值查不到，那个key对应的值就是null
	 * 
	 * @param keys
	 * @return
	 */
	public List<String> mget(final String[] keys) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<List<String>>() {
				public List<String> invoke(Jedis jedis) {
					return jedis.mget(keys);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获取多个key的值，值的顺序跟key一致，如果key对应的值查不到，那个key对应的值就是null
	 * 
	 * @param keys
	 * @return
	 */
	public List<byte[]> mgetByte(final byte[][] keys) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<List<byte[]>>() {
				public List<byte[]> invoke(Jedis jedis) {
					return jedis.mget(keys);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 一次性设置多个key value 已存在则覆盖
	 * 
	 * @param keyValues
	 *            假设数组长度为4，那么内容就是{key0，value0，key1,value1}，也就是2个键值对
	 * @return
	 */
	public void mset(final String[] keyValues) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.mset(keyValues);
					return null;
				}
			});
		}
	}

	/**
	 * 一次性设置多个key value 已存在则返回0
	 * 
	 * @param keyValues
	 *            假设数组长度为4，那么内容就是{key0，value0，key1,value1}，也就是2个键值对
	 * @return
	 */
	public void msetnx(final String[] keyValues) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.msetnx(keyValues);
					return null;
				}
			});
		}
	}

	public String set(final String key, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.set(key, value);
				}
			});
		} else {
			return null;
		}
	}

	public String setByte(final byte[] key, final byte[] value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.set(key, value);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 当key不存在的时候，就设置它，并且设置它的过期时间
	 * 
	 * @param key
	 *            主键
	 * @param value
	 *            值
	 * @param sec
	 *            过期的秒数
	 * @return Status code reply
	 * @author nince
	 */
	public String setNxTTL(final String key, final String value, final int sec) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.set(key, value, "NX", "EX", sec);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 当key不存在的时候，就设置它，并且设置它的过期时间
	 *
	 * @param key
	 *            主键
	 * @param value
	 *            值
	 * @param ttl
	 *            过期的时间数
	 * @param timeUnit
	 *            时间单位
	 * @return Status code reply
	 * @author nince
	 */
	public String setNxTTL(final String key, final String value, final long ttl, final TimeUnit timeUnit) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					long tempttl = ttl;
					if (!timeUnit.equals(TimeUnit.MILLISECONDS)) {
						tempttl = timeUnit.toMillis(ttl);
					}
					return jedis.set(key, value, "NX", "PX", tempttl);
				}
			});
		} else {
			return null;
		}
	}

	public String setNxTTL(final byte[] key, final byte[] value, final int sec) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.set(key, value, "NX".getBytes(), "EX".getBytes(), sec);
				}
			});
		} else {
			return null;
		}
	}

	public String setNxTTL(final byte[] key, final byte[] value, final long ttl, final TimeUnit timeUnit) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					long tempttl = ttl;
					if (!timeUnit.equals(TimeUnit.MILLISECONDS)) {
						tempttl = timeUnit.toMillis(ttl);
					}
					return jedis.set(key, value, "NX".getBytes(), "PX".getBytes(), tempttl);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。
	 * 
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 */
	public Boolean setBit(final String key, final long offset, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Boolean>() {
				public Boolean invoke(Jedis jedis) {
					return jedis.setbit(key, offset, value);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
	 * 
	 * @param key
	 * @param seconds
	 * @param value
	 */
	public void setEx(final String key, final int seconds, final String value) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.setex(key, seconds, value);
					return null;
				}
			});
		}
	}

	@Override
	public void setEx(final byte[] key, final int seconds, final byte[] value) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.setex(key, seconds, value);
					return null;
				}
			});
		}
	}

	/**
	 * @param key
	 * @param value
	 * @return 1 if the key was set 0 if the key was not set
	 */
	public Long setnx(final String key, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.setnx(key, value);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 用 value 参数覆写(overwrite)给定 key 所储存的字符串值，从偏移量 offset 开始。
	 * 
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 */
	public Long setRange(final String key, final long offset, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.setrange(key, offset, value);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回 key 所储存的字符串值的长度。
	 * 
	 * @param key
	 * @return
	 */
	public Long strLen(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.strlen(key);
				}
			});
		} else {
			return null;
		}
	}

	// SCRIPT LOAD "if redis.call('get',KEYS[1])==KEYS[2] then return
	// redis.call('del',KEYS[1]) else return 0 end"
	private static String sha = "ae72f7d7318491f4e6fd0167aa73d6beccc73bc2";

	/**
	 * 值相同则删除
	 * 
	 * @param key
	 * @param value
	 * @return 1 if the key was set 0 if the key was not set
	 */
	public Long delnx(final String key, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return (Long) jedis.evalsha(sha, 2, key, value);
				}
			});
		} else {
			return null;
		}
	}

	// TODO////////////////////Hash（哈希表）/////////////////////////////
	/**
	 * @param bigKey
	 * @param smallKey
	 * @return If the field was present in the hash it is deleted and 1 is
	 *         returned, otherwise 0 is returned and no operation is performed
	 * @author nince
	 */
	public void hdel(final String bigKey, final String... smallKeys) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.hdel(bigKey, smallKeys);
					return null;
				}
			});
		}
	}

	/**
	 * 查看哈希表 key 中，给定域 field 是否存在
	 * 
	 * @param bigKey
	 * @param smallKey
	 * @return
	 */
	public boolean hexists(final String bigKey, final String smallKey) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Boolean>() {
				public Boolean invoke(Jedis jedis) {
					return jedis.hexists(bigKey, smallKey);
				}
			});
		} else {
			return false;
		}
	}

	/**
	 * 返回哈希表 key 中给定域 field 的值。
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public String hget(final String key, final String field) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.hget(key, field);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回map类型的key的所有数据
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, String> hgetAll(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Map<String, String>>() {
				public Map<String, String> invoke(Jedis jedis) {
					return jedis.hgetAll(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 功能描述: <br>
	 * 对map型数据中的二级主键做加法操作，如果value是负数，它就会执行减法操作
	 */
	public long hincr(final String bigKey, final String smallKey, final long value) {
		if (RedisConstants.isInUse()) {
			Long v = getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.hincrBy(bigKey, smallKey, value);
				}
			});
			return v != null ? v.longValue() : -1;
		} else {
			return -1;
		}
	}

	/**
	 * 返回map类型数据的所有二级主键
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> hkeys(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Set<String>>() {
				public Set<String> invoke(Jedis jedis) {
					return jedis.hkeys(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回哈希表 key 中域的数量。
	 * 
	 * @param key
	 * @return
	 */
	public Long hlen(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.hlen(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	public List<String> hmget(final String key, final String[] fields) {
		if (RedisConstants.isInUse()) {
			if (fields == null || fields.length == 0) {
				return new ArrayList<>();
			}
			return getTemplate().executeRead(new JedisCallBackHandler<List<String>>() {
				public List<String> invoke(Jedis jedis) {
					return jedis.hmget(key, fields);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
	 * 
	 * @param key
	 * @param map
	 * @return
	 */
	public String hmset(final String key, final Map<String, String> map) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.hmset(key, map);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
	 * 
	 * @param key1
	 * @param field
	 * @param value
	 */
	public void hset(final String key1, final String field, final String value) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.hset(key1, field, value);
					return null;
				}
			});
		}
	}

	public void hsetBinary(final String key, final String field, final byte[] value) {
		if (RedisConstants.isInUse()) {
			getTemplate().executeWrite(new JedisCallBackHandler<Void>() {
				public Void invoke(Jedis jedis) {
					jedis.hset(SafeEncoder.encode(key), SafeEncoder.encode(field), value);
					return null;
				}
			});
		}
	}

	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
	 * 
	 * @param key1
	 * @param key2
	 * @param value
	 * @return If the field already exists, 0 is returned, otherwise if a new
	 *         field is created 1 is returned.
	 */
	public Long hsetnx(final String key1, final String key2, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.hsetnx(key1, key2, value);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回哈希表 key 中所有域的值。
	 * 
	 * @param key
	 * @return
	 */
	public List<String> hvals(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<List<String>>() {
				public List<String> invoke(Jedis jedis) {
					return jedis.hvals(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 命令用于迭代哈希键中的键值对
	 * 
	 * @param key
	 * @param cursor
	 * @param count
	 * @param patterns
	 * @return
	 */
	public ScanResult<Entry<String, String>> hscan(final String key, final String cursor, final Integer count,
			final String... patterns) {
		ScanParams params = new ScanParams();
		for (String pattern : patterns) {
			params.match(pattern);
		}
		if (null != count) {
			params.count(count);
		}
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<ScanResult<Entry<String, String>>>() {
				public ScanResult<Entry<String, String>> invoke(Jedis jedis) {
					return jedis.hscan(key, cursor, params);
				}
			});
		} else {
			return null;
		}
	}

	public Map<String, String> getMap(String key) {
		if (RedisConstants.isInUse()) {
			String json = this.get(key);
			if (json == null) {
				return null;
			}
			return JsonUtil.parseMap(json);
		} else {
			return null;
		}
	}

	public void setMap(String key, Map<String, String> map) {
		if (RedisConstants.isInUse()) {
			String json;
			json = JsonUtil.toJson(map);
			this.set(key, json);
		}
	}

	public Map<String, String> getMap(String key1, String key2) {
		if (RedisConstants.isInUse()) {
			String json = this.hget(key1, key2);
			if (json == null) {
				return null;
			}
			return JsonUtil.parseMap(json);
		} else {
			return null;
		}
	}

	public void setMap(String key1, String key2, Map<String, String> map) {
		if (RedisConstants.isInUse()) {
			String json;
			json = JsonUtil.toJson(map);
			this.hset(key1, key2, json);
		}
	}

	public void setExMap(String key, int seconds, Map<String, String> map) {
		if (RedisConstants.isInUse()) {
			String json;
			json = JsonUtil.toJson(map);
			this.setEx(key, seconds, json);
		}
	}

	// TODO////////////////////List（列表）队列左进右出，堆栈左进左出/////////////////////////////
	/**
	 * @param key
	 * @param values
	 * @return 队列
	 * @author nince
	 */
	public List<String> bpull(final String... key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<List<String>>() {
				public List<String> invoke(Jedis jedis) {
					return jedis.brpop(30, key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * @param key
	 * @param values
	 * @return 队列
	 * @author nince
	 */
	public List<byte[]> bpull(final byte[]... key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<List<byte[]>>() {
				public List<byte[]> invoke(Jedis jedis) {
					return jedis.brpop(30, key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 从列表的左边(表头)移除数据
	 * 
	 * @param key
	 * @param count
	 *            要移除的数目
	 * @return OK 表示成功，其它都是失败
	 * @author nince
	 */
	public boolean removeLeft(final String key, final int count) {
		if (RedisConstants.isInUse()) {
			String ret = getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					// 如果count比list的len还要大，就会清楚列表的所有数据
					return jedis.ltrim(key, count, -1);
				}
			});
			return StringUtils.equals(ret, "OK");
		} else {
			return false;
		}
	}

	/**
	 * @param key
	 * @param values
	 * @return 插入后队列的长度
	 * @author nince
	 */
	public Long push(final String key, final String... values) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.rpush(key, values);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * @param key
	 * @param values
	 * @return 插入后队列的长度
	 * @author nince
	 */
	public Long push(final byte[] key, final byte[]... values) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.rpush(key, values);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * @param key
	 * @param values
	 * @return 队列
	 * @author nince
	 */
	public String queuePull(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.lpop(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * @param key
	 * @param values
	 * @return 队列
	 * @author nince
	 */
	public String storePop(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.rpop(key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回列表的前count个元素
	 * 
	 * @param key
	 * @param count
	 * @return
	 * @author nince
	 */
	public List<String> lrange(final String key, final int count) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<List<String>>() {
				public List<String> invoke(Jedis jedis) {
					// end下标包含end本身
					return jedis.lrange(key, 0, count - 1);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 返回列表的数目
	 * 
	 * @param key
	 * @return
	 * @author nince
	 */
	public Long llen(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					// end下标包含end本身
					return jedis.llen(key);
				}
			});
		} else {
			return null;
		}
	}
	// TODO////////////////////Set（集合）/////////////////////////////

	/**
	 * 添加一个member到集合中去
	 * 
	 * @param key
	 * @param member
	 * @return 1 if the new element was added 0 if the element was already a
	 *         member of the set
	 */
	public Long sadd(final String key, final String member) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.sadd(key, member);
				}
			});
		} else {
			return null;
		}
	}

	public Long srem(final String key, final String member) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.srem(key, member);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 添加多个member到集合中去
	 * 
	 * @param key
	 * @param member
	 * @return 1 if the new element was added 0 if the element was already a
	 *         member of the set
	 */
	public Long sadd(final String key, final String[] members) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.sadd(key, members);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 设置set类型的值
	 * 
	 * @param key
	 * @param member
	 * @return 1 if the new element was added 0 if the element was already a
	 *         member of the set
	 */
	public Long sSet(final String key, final String[] members) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Transaction trans = jedis.multi();
					trans.del(key);
					Long ret = jedis.sadd(key, members);
					trans.exec();
					return ret;
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获取set的所有值
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> smembers(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Set<String>>() {
				public Set<String> invoke(Jedis jedis) {
					return jedis.smembers(key);
				}
			});
		} else {
			return null;
		}
	}

	// TODO////////////////////SortedSet（有序集合）/////////////////////////////
	/**
	 * 设置set类型的值
	 * 
	 * @param key
	 * @param member
	 * @return Integer reply, specifically: 1 if the new element was added 0 if
	 *         the element was already a member of the sorted set and the score
	 *         was updated
	 */
	public Long zadd(final String key, final String member, final double score) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.zadd(key, score, member);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 删除有序集合中的内容
	 * 
	 * @param key
	 * @param members
	 * @return
	 */
	public Long zdel(final String key, final String... members) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.zrem(key, members);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获取有序集合中下标startIndex到endIndex的所有元素。 按score从小到大排序
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> zRange(final String key, final long startIndex, final long endIndex) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Set<String>>() {
				public Set<String> invoke(Jedis jedis) {
					return jedis.zrange(key, startIndex, endIndex);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获取有序集合中下标startIndex到endIndex的所有元素。 按score从大到小排序
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> zRangeDesc(final String key, final long startIndex, final long endIndex) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Set<String>>() {
				public Set<String> invoke(Jedis jedis) {
					return jedis.zrevrange(key, startIndex, endIndex);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获取有序集合中score在min和max之间的所有元素。
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> zRangeByScore(final String key, final double min, final double max) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<Set<String>>() {
				public Set<String> invoke(Jedis jedis) {
					return jedis.zrangeByScore(key, min, max);
				}
			});
		} else {
			return null;
		}
	}

	public String ping() {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return jedis.ping();
				}
			});
		} else {
			return null;
		}
	}

	public Object evalsha(final String sha1, final int keyCount, final String... params) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Object>() {
				public Object invoke(Jedis jedis) {
					return jedis.evalsha(sha1, keyCount, params);
				}
			});
		} else {
			return null;
		}
	}

	public Long publish(final String channel, final String message) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					return jedis.publish(channel, message);
				}
			});
		} else {
			return null;
		}
	}

	private static String sha_setIfEqual = "825bb0fe482721bf0016852c360cada99e43ed48";

	public String setIfEqual(final String key, final String oldValue, final String newValue) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<String>() {
				public String invoke(Jedis jedis) {
					return (String) jedis.evalsha(sha_setIfEqual, 3, key, oldValue, newValue);
				}
			});
		} else {
			return null;
		}
	}

	private static String sha_listAllValue = "6add22817d60b1bea3c7d91421b1142c8fbe09f6";

	/**
	 * @param key
	 * @param value
	 * @return
	 */
	public List<String> listAllValue(final String key) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeRead(new JedisCallBackHandler<List<String>>() {
				@SuppressWarnings("unchecked")
				public List<String> invoke(Jedis jedis) {
					return (List<String>) jedis.evalsha(sha_listAllValue, 1, key);
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 原子释放锁
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Long releaseNoneReentrantLock(final String key, final String value) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("releaseNoneReentrantLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 2, key, value);
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 2, key, value);
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 获取全局锁
	 * 
	 * @param key
	 * @param value
	 * @param ttl
	 * @return
	 */
	public Long getGlobalReentrantLock(final String key, final String value, final long level, final int ttl) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("getGlobalReentrantLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 4, key, value, String.valueOf(level), String.valueOf(ttl));
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 4, key, value, String.valueOf(level), String.valueOf(ttl));
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 释放全局锁
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Long releaseGlobalReentrantLock(final String key, final String value, final long level) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("releaseGlobalReentrantLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 3, key, value, String.valueOf(level));
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 3, key, value, String.valueOf(level));
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 释放请求链所有锁
	 * 
	 * @param key
	 * @return
	 */
	public Long releaseChainLock(String chainId) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("releaseChainLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 1, chainId);
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 1, chainId);
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 排队获取锁
	 * 
	 * @param key
	 * @return
	 */
	public Long queueGetGlobalReentrantLock(final String key, final String value, final long level, final int ttl) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("queueGetGlobalReentrantLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 4, key, value, String.valueOf(level), String.valueOf(ttl));
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 4, key, value, String.valueOf(level), String.valueOf(ttl));
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 解锁
	 * 
	 * @param key
	 * @return
	 */
	public Long unLockGlobalReentrantLock(final String key, final String value, final long level) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("unLockGlobalReentrantLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 3, key, value, String.valueOf(level));
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 3, key, value, String.valueOf(level));
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 唤醒得锁
	 * 
	 * @param key
	 * @return
	 */
	public Long callGetGlobalReentrantLock(final String key, final String value, final int ttl) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("callGetGlobalReentrantLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 3, key, value, String.valueOf(ttl));
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 3, key, value, String.valueOf(ttl));
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}

	/**
	 * 竞争获取锁
	 * 
	 * @param key
	 * @return
	 */
	public Long competeGetGlobalReentrantLock(final String key, final String value, final String oldValue,
			final int ttl) {
		if (RedisConstants.isInUse()) {
			return getTemplate().executeWrite(new JedisCallBackHandler<Long>() {
				public Long invoke(Jedis jedis) {
					Script script = ScriptCache.getScript("competeGetGlobalReentrantLock");
					String sha = script.getSha();
					if (null == sha) {
						sha = jedis.scriptLoad(script.getLuaScript());
						script.setSha(sha);
					}
					try {
						return (Long) jedis.evalsha(sha, 4, key, value, oldValue, String.valueOf(ttl));
					} catch (JedisDataException e) {
						if (e.getMessage().equals("NOSCRIPT No matching script. Please use EVAL.")) {
							jedis.scriptLoad(script.getLuaScript());
							return (Long) jedis.evalsha(sha, 4, key, value, oldValue, String.valueOf(ttl));
						}
						return null;
					}
				}
			});
		} else {
			return null;
		}
	}
}
