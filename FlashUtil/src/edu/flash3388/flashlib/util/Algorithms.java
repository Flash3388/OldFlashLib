package edu.flash3388.flashlib.util;

public final class Algorithms {
	private Algorithms(){}
	
	public static interface VoidAction<T>{
		void execute(T object);
	}
	public static interface Action<T, V>{
		V execute(T object);
	}
	
	public static <T> TreeNode<T> minMax(TreeNode<T> head, 
			Action<T, ?extends TreeNode<T>> action, int maxDepth){
		return minMax(head, action, 0, maxDepth);
	}
	private static <T> TreeNode<T> minMax(TreeNode<T> head, 
			Action<T, ?extends TreeNode<T>> action, int depth, int maxDepth){
		if(depth >= maxDepth){
			
		}
		
		return head;
	}
}
