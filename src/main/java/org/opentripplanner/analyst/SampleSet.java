package org.opentripplanner.analyst;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.routing.graph.Vertex;

/**
 * We never use samples in isolation, so let's store them as a column store.
 */
public class SampleSet {

    public final PointSet pset;

    /* Vertices at the two ends of a road, one per sample. */
    Vertex[] v0s;
    Vertex[] v1s;

    /* Distances to the vertices at the two ends of a road, one per sample. */
    float[] t0s;
    float[] t1s;

    public SampleSet (PointSet pset, SampleFactory sfac) {
        this.pset = pset;
        v0s = new Vertex[pset.capacity];
        v1s = new Vertex[pset.capacity];
        t0s = new float[pset.capacity];
        t1s = new float[pset.capacity];
        for (int i = 0; i < pset.capacity; i++) {
            Sample sample = sfac.getSample(pset.lons[i], pset.lats[i]);
            if (sample == null) {
                t0s[i] = Float.NaN;
                t1s[i] = Float.NaN;
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

}
