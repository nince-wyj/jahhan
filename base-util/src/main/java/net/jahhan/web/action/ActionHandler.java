package net.jahhan.web.action;

import net.jahhan.handler.WorkHandler;


public abstract class ActionHandler implements WorkHandler{
	protected ActionHandler nextHandler;
}