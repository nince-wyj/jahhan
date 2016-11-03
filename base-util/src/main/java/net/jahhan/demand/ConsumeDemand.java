package net.jahhan.demand;

public interface ConsumeDemand {

	public void addActionConsume(String actName, long costTime);

	public String getActionConsumeMessage(String actName, Integer day,Integer statistics);
}
