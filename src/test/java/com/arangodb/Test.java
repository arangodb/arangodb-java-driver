package com.arangodb;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Test {

	public static void main(final String[] args) {

		System.out.println(Thread.currentThread().getId());
		System.out.println("before");
		final CompletableFuture<String> future = doSomething();
		// future.thenAccept(s -> {
		// System.out.println(s);
		// });
		future.thenAccept(new Consumer<String>() {
			@Override
			public void accept(final String t) {
				System.out.println(Thread.currentThread().getId());
				System.out.println(t);
			}
		});
		System.out.println("after");
		try {
			future.get();
		} catch (final InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static CompletableFuture<String> doSomething() {
		final CompletableFuture<String> future = new CompletableFuture<>();
		final Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(Thread.currentThread().getId());
				future.complete("Hallo");
			}
		};
		t.start();
		try {
			t.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getId());
		// final CompletableFuture<String> future =
		// CompletableFuture.supplyAsync(() -> {
		// try {
		// System.out.println("started");
		// System.out.println(Thread.currentThread().getId());
		// Thread.sleep(1000);
		// } catch (final InterruptedException e) {
		// System.out.println("outch!");
		// }
		// return "Hallo";
		// });
		return future;
	}

}
