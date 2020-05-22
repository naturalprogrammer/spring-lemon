package com.naturalprogrammer.spring.lemon.commonsjpa;

import com.naturalprogrammer.spring.lemon.exceptions.VersionException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.util.Objects;

public class LecjUtils {

	/**
	 * A convenient method for running code
	 * after successful database commit.
	 *  
	 * @param runnable
	 */
	public static void afterCommit(Runnable runnable) {
		
		TransactionSynchronizationManager.registerSynchronization(
		    new TransactionSynchronizationAdapter() {
		        @Override
		        public void afterCommit() {
		        	
		        	runnable.run();
		        }
		});				
	}


	/**
	 * Throws a VersionException if the versions of the
	 * given entities aren't same.
	 * 
	 * @param original
	 * @param updated
	 */
	public static <ID extends Serializable>
	void ensureCorrectVersion(LemonEntity<ID> original, LemonEntity<ID> updated) {
		
		if (!Objects.equals(original.getVersion(), updated.getVersion()))
			throw new VersionException(original.getClass().getSimpleName(), original.getId().toString());
	}
}
