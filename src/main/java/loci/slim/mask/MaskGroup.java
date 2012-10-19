//
// MaskGroup.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.mask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mask group class associates a set of mask nodes.
 * <p>
 * Note that mask changes are a results of user interaction using the single
 * histogram tool, so threading issues are unlikely.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class MaskGroup implements IMaskGroup {
    List<IMaskNode> _nodeList;
    Map<IMaskNode, Mask> _maskMap;
    IMaskNode[] _nodes;

    public MaskGroup() {
        _nodeList = new ArrayList<IMaskNode>();
        _maskMap = new HashMap<IMaskNode, Mask>();
    }

    @Override
    public void addNode(MaskNode node) {
		// avoid duplicate entries
		if (!_nodeList.contains(node)) {
            _nodeList.add(node);
		}
    }

    @Override
    public void removeNode(MaskNode node) {
        _nodeList.remove(node);
        _maskMap.put(node, null);
    }

    @Override
    public void updateMask(IMaskNode node) {
        // update map with node's new self mask
        _maskMap.put(node, node.getSelfMask());

        // combine masks and notify other nodes
        for (IMaskNode peerNode : _nodeList) {
            // skip the caller
            if (peerNode != node) {
                // combine all masks but the recipient's
                Mask peerSelfMask = _maskMap.get(peerNode);
                Mask peerOtherMask = Mask.addOtherMasks(peerSelfMask, _maskMap.values());
                peerNode.updateOtherMask(peerOtherMask);
            }
        }
    }
	
	@Override
	public Mask getMask() {
		return Mask.addOtherMasks(null, _maskMap.values());
	}
}
