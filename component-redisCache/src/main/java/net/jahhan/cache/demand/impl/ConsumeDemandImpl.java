//package net.jahhan.cache.demand.impl;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import net.jahhan.cache.Redis;
//import net.jahhan.cache.RedisConfigurationManger;
//import net.jahhan.cache.RedisConstants;
//import net.jahhan.cache.RedisFactory;
//import net.jahhan.cache.repository.common.LockRepository;
//import net.jahhan.context.BaseContext;
//import net.jahhan.demand.ConsumeDemand;
//import net.jahhan.web.action.ServiceRegisterHelper;
//
//public class ConsumeDemandImpl implements ConsumeDemand {
//	private final static Logger logger = LoggerFactory.getLogger("ConsumeServiceImpl");
//	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//
//	private static Redis redis = RedisFactory.getRedis(RedisConstants.TABLE_COMMON, null);
//
//	private final String CONSUME_DAY = "consume_day_";
//	private final String CONSUME_DAY_HIT = "consume_dayHit_";
//	private final String CONSUME_DAY_TIME = "consume_dayTime_";
//	private final String CONSUME_PREDAY_HIT = "consume_predayHit_";
//	private final String CONSUME_PREDAY_TIME = "consume_predayTime_";
//	private final String LOCK_PRE = "consume_";
//
//	@Override
//	public void addActionConsume(String actName, long costTime) {
//		Integer today = Integer.valueOf(sdf.format(new Date()));
//		if (RedisConfigurationManger.isInUse()) {
//			Integer day = null;
//			String dayS = redis.get(CONSUME_DAY + actName);
//			if (null != dayS)
//				day = Integer.valueOf(dayS);
//			if (null == day) {
//				if (LockRepository.lock(LOCK_PRE + actName)) {
//					try {
//						redis.set(CONSUME_DAY + actName, Integer.toString(today));
//						redis.set(CONSUME_DAY_HIT + actName, "1");
//						redis.set(CONSUME_DAY_TIME + actName, Long.toString(costTime));
//					} catch (Exception e) {
//						logger.error("", e);
//					} finally {
//						LockRepository.releaseLock(LOCK_PRE + actName);
//					}
//				}
//				return;
//			}
//			if (day.equals(today)) {
//				if (LockRepository.lock(LOCK_PRE + actName)) {
//					try {
//						redis.incr(CONSUME_DAY_HIT + actName);
//						redis.incrBy(CONSUME_DAY_TIME + actName, costTime);
//					} catch (Exception e) {
//						logger.error("", e);
//					} finally {
//						LockRepository.releaseLock(LOCK_PRE + actName);
//					}
//				}
//				return;
//			}
//			if (day < today) {
//				if (LockRepository.lock(LOCK_PRE + actName)) {
//					try {
//						Integer hit = Integer.valueOf(redis.get(CONSUME_DAY_HIT + actName));
//						Long time = Long.valueOf(redis.get(CONSUME_DAY_TIME + actName));
//						redis.incrBy(CONSUME_DAY_HIT + actName, -hit + 1);
//						redis.incrBy(CONSUME_DAY_TIME + actName, -time + costTime);
//						redis.set(CONSUME_DAY + actName, Integer.toString(today));
//						if (RedisConfigurationManger.isConsumePersisdence()) {
//							// cachePersisdence(actName, costTime, day, hit);
//							return;
//						}
//						int cacheSeconds = RedisConfigurationManger.getConsumeCacheDay() * 24 * 60 * 60;
//						redis.set(CONSUME_PREDAY_HIT + actName + ":" + day, Integer.toString(hit));
//						redis.expire(CONSUME_PREDAY_HIT + actName + ":" + day, cacheSeconds);
//						redis.set(CONSUME_PREDAY_TIME + actName + ":" + day, Long.toString(costTime));
//						redis.expire(CONSUME_PREDAY_TIME + actName + ":" + day, cacheSeconds);
//					} catch (Exception e) {
//						logger.error("", e);
//					} finally {
//						LockRepository.releaseLock(LOCK_PRE + actName);
//					}
//				}
//				return;
//			}
//			return;
//		}
//		addActionConsume(actName, costTime);
//	}
//
//	@Override
//	public String getActionConsumeMessage(String actName, Integer day, Integer statistics) {
//		Integer today = Integer.valueOf(sdf.format(new Date()));
//		StringBuilder sb = new StringBuilder();
//		if (RedisConfigurationManger.isInUse()) {
//			if (null != statistics) {
//				List<Integer> dateList = new ArrayList<>();
//				for (int i = 1; i < statistics + 1; i++) {
//					dateList.add(Integer.valueOf(sdf.format(new Date(new Date().getTime() - i * 24 * 60 * 60 * 1000))));
//				}
//				List<String> serviceNameList = BaseContext.CTX.getInjector()
//						.getInstance(ServiceRegisterHelper.class).getAllServiceName();
//				Comparator<Integer> comparator = new Comparator<Integer>() {
//					@Override
//					public int compare(Integer o1, Integer o2) {
//						return o2.compareTo(o1);
//					}
//				};
//				Map<Integer, String> respMessage = new TreeMap<>(comparator);
//				for (int i = 0; i < serviceNameList.size(); i++) {
//					String serviceName = serviceNameList.get(i);
//					Integer hit = 0;
//					Integer time = 0;
//					String dayS = redis.get(CONSUME_DAY + serviceName);
//					if (null != dayS && dayS.equals(today.toString())) {
//						String hitString = redis.get(CONSUME_DAY_HIT + serviceName);
//						hit = Integer.valueOf(null == hitString ? "0" : hitString);
//						String timeString = redis.get(CONSUME_DAY_TIME + serviceName);
//						time = Integer.valueOf(null == timeString ? "0" : timeString);
//					}
//					for (int j = 0; j < dateList.size(); j++) {
//						String hitString = redis.get(CONSUME_PREDAY_HIT + serviceName + ":" + dateList.get(j));
//						hit = hit + Integer.valueOf(null == hitString ? "0" : hitString);
//						String timeString = redis.get(CONSUME_PREDAY_TIME + serviceName + ":" + dateList.get(j));
//						time = time + Integer.valueOf(null == timeString ? "0" : timeString);
//					}
//					int resTime = hit == 0 ? 0 : time / hit;
//					StringBuilder s = new StringBuilder();
//					s.append("接口名：").append(serviceName).append("在").append(statistics).append("天内的点击次数：").append(hit)
//							.append(" 平均响应速度：").append(resTime).append(" ms\n");
//					if (respMessage.containsKey(resTime)) {
//						s.append(respMessage.get(resTime));
//					}
//					respMessage.put(resTime, s.toString());
//				}
//				Iterator<Integer> respIt = respMessage.keySet().iterator();
//				while (respIt.hasNext()) {
//					sb.append(respMessage.get(respIt.next()));
//				}
//				return sb.toString();
//			}
//			if (null != day && day.equals(today)) {
//				Iterator<String> hitKeyIt = redis.keys(CONSUME_DAY_HIT + "*").iterator();
//				while (hitKeyIt.hasNext()) {
//					String hitKey = hitKeyIt.next();
//					String dayS = redis.get(hitKey.replace(CONSUME_DAY_HIT, CONSUME_DAY));
//					Integer hitday = 0;
//					if (null != dayS)
//						hitday = Integer.valueOf(dayS);
//					if (!hitday.equals(day))
//						continue;
//					String hitS = redis.get(hitKey);
//					String timeS = redis.get(hitKey.replace(CONSUME_DAY_HIT, CONSUME_DAY_TIME));
//					int hit = 0;
//					if (null != hitS)
//						hit = Integer.valueOf(hitS);
//					int time = 0;
//					if (null != timeS)
//						time = Integer.valueOf(timeS);
//					appendMessageSt(sb, hitKey.replace(CONSUME_DAY_HIT, ""), day, hit, time);
//				}
//				return sb.toString();
//			}
//			if (!RedisConfigurationManger.isConsumePersisdence()) {
//				if (null != day && day < today) {
//					Iterator<String> hitKeyIt = redis.keys(CONSUME_PREDAY_HIT + "*:" + day).iterator();
//					while (hitKeyIt.hasNext()) {
//						String hitKey = hitKeyIt.next();
//						String hitS = redis.get(hitKey);
//						String timeS = redis.get(hitKey.replace(CONSUME_PREDAY_HIT, CONSUME_PREDAY_TIME));
//						int hit = 0;
//						if (null != hitS)
//							hit = Integer.valueOf(hitS);
//						int time = 0;
//						if (null != timeS)
//							time = Integer.valueOf(timeS);
//						appendMessageSt(sb, hitKey.replace(CONSUME_PREDAY_HIT, "").replace(":" + day, ""), day, hit,
//								time);
//					}
//					return sb.toString();
//				}
//				if (null != actName) {
//					Iterator<String> hitKeyIt = redis.keys(CONSUME_PREDAY_HIT + actName + ":*").iterator();
//					while (hitKeyIt.hasNext()) {
//						String hitKey = hitKeyIt.next();
//						String hitS = redis.get(hitKey);
//						String timeS = redis.get(hitKey.replace(CONSUME_PREDAY_HIT, CONSUME_PREDAY_TIME));
//						int hit = 0;
//						if (null != hitS)
//							hit = Integer.valueOf(hitS);
//						int time = 0;
//						if (null != timeS)
//							time = Integer.valueOf(timeS);
//						appendMessageSt(sb, actName,
//								Integer.valueOf(hitKey.replace(CONSUME_PREDAY_HIT + actName + ":", "")), hit, time);
//					}
//					return sb.toString();
//				}
//				return sb.toString();
//			}
//			// DBConnExecutorFactory connExec = new DBConnExecutorFactory(
//			// DBConnectionType.READ);
//			// try {
//			// connExec.beginConnection();
//			// List<FwActionConsumePreday> fwActionConsumePredayList = null;
//			// if (null != day && day < today)
//			// fwActionConsumePredayList = fwActionConsumePredayDao
//			// .listFwActionConsumePredayByDay(day);
//			// else if (null != actName)
//			// fwActionConsumePredayList = fwActionConsumePredayDao
//			// .listFwActionConsumePredayByActName(actName);
//			// for (int i = 0; i < fwActionConsumePredayList.size(); i++) {
//			// FwActionConsumePreday fwActionConsumePreday =
//			// fwActionConsumePredayList
//			// .get(i);
//			// appendMessageSt(sb, fwActionConsumePreday.getActName(),
//			// fwActionConsumePreday.getDay(),
//			// fwActionConsumePreday.getHitCount(),
//			// fwActionConsumePreday.getResponseTime());
//			// }
//			// connExec.endConnection();
//			// } catch (Exception e) {
//			// logger.error("DBConnHandler error {}", e);
//			// connExec.rollback();
//			// } catch (Error e) {
//			// logger.error("DBConnHandler error {}", e);
//			// connExec.rollback();
//			// } finally {
//			// connExec.close();
//			// }
//			return sb.toString();
//		}
//		return getActionConsumeMessage(actName, day, statistics);
//	}
//	private void appendMessageSt(StringBuilder sb, String actName, Integer day,
//			Integer hit, Integer time) {
//		sb.append("接口名：").append(actName).append("在").append(day)
//				.append(" 点击次数：").append(hit).append(" 平均响应速度：")
//				.append(hit == 0 ? 0 : time / hit).append(" ms\n");
//	}
//	// private void cachePersisdence(String actName, long costTime, Integer day,
//	// Integer hit) {
//	// DBConnExecutorFactory connExec = new DBConnExecutorFactory(
//	// DBConnectionType.WEAK_WRITE);
//	// try {
//	// connExec.beginConnection();
//	// FwActionConsumeAll fwActionConsumeAll = new FwActionConsumeAll();
//	// fwActionConsumeAll.setActName(actName);
//	// fwActionConsumeAll.setAllHitCount(hit);
//	// fwActionConsumeAll.setAllResponseTime(Long.valueOf(costTime)
//	// .intValue());
//	// fwActionConsumeAllDao
//	// .updatePartFwActionConsumeAll(fwActionConsumeAll);
//	//
//	// FwActionConsumePreday fwActionConsumePreday = new
//	// FwActionConsumePreday();
//	// fwActionConsumePreday.setActName(actName);
//	// fwActionConsumePreday.setDay(day);
//	// fwActionConsumePreday.setHitCount(hit);
//	// fwActionConsumePreday.setResponseTime(Long.valueOf(costTime)
//	// .intValue());
//	// fwActionConsumePredayDao
//	// .addFwActionConsumePreday(fwActionConsumePreday);
//	// connExec.endConnection();
//	// } catch (Exception e) {
//	// logger.error("DBConnHandler error {}", e);
//	// connExec.rollback();
//	// } catch (Error e) {
//	// logger.error("DBConnHandler error {}", e);
//	// connExec.rollback();
//	// } finally {
//	// connExec.close();
//	// }
//	// }
//}
