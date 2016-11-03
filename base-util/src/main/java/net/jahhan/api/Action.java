package net.jahhan.api;

public interface Action {
	void execute(RequestMessage requestMessage, ResponseMessage responseMessage);
}