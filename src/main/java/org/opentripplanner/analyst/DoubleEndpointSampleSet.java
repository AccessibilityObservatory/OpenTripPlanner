package org.opentripplanner.analyst;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.analyst.core.SampleSource;
import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.routing.graph.Vertex;

/**
 * We never use samples in isolation, so let's store them as a column store.
 */
public class DoubleEndpointSampleSet implements SampleSet {

    public final PointSet pset;

    /* Vertices at the two ends of a road, one per sample. */
    protected Vertex[] v0s;
    protected Vertex[] v1s;

    /* Times to the vertices at the two ends of a road, one per sample. */
    protected int[] t0s;
    protected int[] t1s;

    public DoubleEndpointSampleSet (PointSet pset, SampleSource sfac) {
        this.pset = pset;
        v0s = new Vertex[pset.capacity];
        v1s = new Vertex[pset.capacity];
        t0s = new int[pset.capacity];
        t1s = new int[pset.capacity];
        for (int i = 0; i < pset.capacity; i++) {
            Sample sample = sfac.getSample(pset.lons[i], pset.lats[i]);
            if (sample == null) {
                t0s[i] = Integer.MAX_VALUE;
                t1s[i] = Integer.MAX_VALUE;
                continue;
            }
            v0s[i] = sample.v0;
            v1s[i] = sample.v1;
            t0s[i] = sample.t0;
            t1s[i] = sample.t1;
        }
    }

    public int[] eval (TimeSurface surf) {
        int[] ret = new int[pset.capacity];
        for (int i = 0; i < pset.capacity; i++) {
            int m0 = Integer.MAX_VALUE;
            int m1 = Integer.MAX_VALUE;
            if (v0s[i] != null) {
                int s0 = surf.getTime(v0s[i]);
                if (s0 != TimeSurface.UNREACHABLE) {
                    m0 = (int) (s0 + t0s[i]);
                }
            }
            if (v1s[i] != null) {
                int s1 = surf.getTime(v1s[i]);
                if (s1 != TimeSurface.UNREACHABLE) {
                    m1 = (int) (s1 + t1s[i]);
                }
            }
            ret[i] = (m0 < m1) ? m0 : m1;
        }
        return ret;
    }
    
    public PointSet getPointSet() {
    	return pset;
    }

}
