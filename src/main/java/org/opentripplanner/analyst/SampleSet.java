package org.opentripplanner.analyst;

public interface SampleSet {
	
	public int[] eval (TimeSurface surf);
	
	public PointSet getPointSet();

}
