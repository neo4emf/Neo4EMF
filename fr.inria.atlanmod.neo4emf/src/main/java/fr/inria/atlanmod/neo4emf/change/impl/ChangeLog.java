package fr.inria.atlanmod.neo4emf.change.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.atlanmod.neo4emf.INeo4emfResource;
import fr.inria.atlanmod.neo4emf.change.IChangeLog;

public class ChangeLog implements IChangeLog<Entry> {
		
	private List<Entry> changes = new ArrayList<Entry>();
	private int size;
	private INeo4emfResource resource;
	
	public ChangeLog(INeo4emfResource resource, int size) {
		this.size = size;
		this.resource = resource;
	}

	@Override
	public boolean add(Entry entry) {
		boolean added = changes.add(entry);
		if(size() == size) {
			this.resource.dirtySave();
		}
		return added;
	}
	
	@Override
	public Iterator<Entry> iterator() {
		return changes.iterator();
	}

	@Override
	public void clear() {
		changes.clear();
	}

	@Override
	public int size() {
		return changes.size();
	}

	@Override
	public List<Entry> changes() {
		return changes;
	}
}
