/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.vertextype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;

/**
 * Represents an ordinary location in space, typically an intersection.
 */
public class IntersectionVertex extends StreetVertex {

    private static final long serialVersionUID = 1L;

    /**
     * Does this intersection have a traffic light?
     */
    public boolean trafficLight;

    /**
     * Is this a free-flowing intersection, i.e. should it have no delay at all? e.g., freeway ramps, &c.
     */
    public boolean freeFlowing;
    
    /** The LTS level of this edge, 1-4. 0 indicates undefined. */
    public byte LTSscore = 0;

    /** Returns true if this.freeFlowing or if it appears that this vertex is free-flowing */
    public boolean inferredFreeFlowing() {
        if (this.freeFlowing) {
            return true;
        }
        
        return getDegreeIn() == 1 && getDegreeOut() == 1 && !this.trafficLight;
    }

    public IntersectionVertex(Graph g, String label, double x, double y, String name) {
        super(g, label, x, y, name);
        freeFlowing = false;
        trafficLight = false;
    }

    public IntersectionVertex(Graph g, String label, double x, double y) {
        this(g, label, x, y, label);
    }
    
    /**
     * Get a collection containing all the edges leading from this vertex, filtered to 
     * reflect whether this vertex itself is traversable according to the specified routing
     * request.
     */
    @Override
    public Collection<Edge> getOutgoing(RoutingRequest options) {
    	// Check traversability of this intersection based on LTS score
    	if (!options.intersectionLTS || (options.intersectionLTS && (LTSscore <= options.maxLTS))) {
    		return this.getOutgoing();
    	} else {
    		return new ArrayList<Edge>(0);
    	}
    	
    }
    
    public Collection<Edge> getIncoming(RoutingRequest options) {
    	// Check traversability of this intersection based on LTS score
    	if (!options.intersectionLTS || (options.intersectionLTS && (LTSscore <= options.maxLTS))) {
    		return this.getIncoming();
    	} else {
    		return new ArrayList<Edge>(0);
    	}
    	
    }
    

}
