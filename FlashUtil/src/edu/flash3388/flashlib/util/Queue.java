package edu.flash3388.flashlib.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class Queue<T> implements java.util.Queue<T>{

	private Object[] elements;
	private int nextIndex = 0;
	
	public Queue(int size){
		if(size < 1)
			throw new IllegalArgumentException("Start size must be atleast 1");
		elements = new Object[size];
	}
	public Queue(){
		this(10);
	}
	
	public int size(){
		return nextIndex;
	}
	public int capacity(){
		return elements.length;
	}
	public boolean isEmpty(){
		return size() == 0;
	}
	
	public void enqueue(T element){
		checkSize();
		elements[nextIndex++] = element;
	}
	public T dequeue(){
		if(isEmpty()) return null;
		T element = (T) elements[0];
		shift();
		nextIndex--;
		return element;
	}
	public T peek(){
		if(isEmpty()) return null;
		return (T) elements[0];
	}
	public void clear(){
		for (int i = 0; i < size(); i++) 
			elements[i] = null;
		nextIndex = 0;
	}
	
	private void shift(){
		shift(0);
	}
	private void shift(int index){
		for(int i = 0; i < size()-1; i++){
			elements[i] = elements[i+1];
			elements[i+1] = null;
		}
	}
	private void checkSize(){
		if(size() < capacity()) return;
		Object[] newarr = new Object[capacity() + 10];
		System.arraycopy(elements, 0, newarr, 0, capacity());
		elements = newarr;
	}
	
	@Override
	public boolean contains(Object o) {
		for(int i = 0; i < size(); i++){
			if(o.equals(elements[i]))
				return true;
		}
		return false;
	}
	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object[] toArray() {
		return Arrays.copyOfRange(elements, 0, size());
	}
	
	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		T[] ret = null;
		if(a != null && a.length >= size()){
			ret = a;
			System.arraycopy(elements, 0, ret, 0, size());
		}else
			ret = (T[]) Arrays.copyOf(elements, size());
		return ret;
	}
	@Override
	public boolean remove(Object o) {
		for (int i = 0; i < size(); i++) {
			if(elements[i].equals(o)){
				shift(i);
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}
	@Override
	public boolean addAll(Collection<? extends T> c) {
		return false;
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}
	@Override
	public boolean add(T e) {
		if(size() < capacity()){
			elements[nextIndex++] = e;
			return true;
		}
		throw new IllegalStateException("Queue is full");
	}
	@Override
	public boolean offer(T e) {
		if(size() < capacity()){
			elements[nextIndex++] = e;
			return true;
		}
		return false;
	}
	@Override
	public T remove() {
		if (isEmpty())
			throw new IllegalStateException("Queue is empty");
		return dequeue();
	}
	@Override
	public T poll() {
		return dequeue();
	}
	@Override
	public T element() {
		return peek();
	}
}