package com.integralblue.callerid;

public interface CallerIDLookup {
	public class NoResultException extends Exception{
		private static final long serialVersionUID = 2604974702552015288L;
	}
	
	CallerIDResult lookup(CharSequence phoneNumber) throws NoResultException;
}
