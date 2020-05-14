package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.util.log.Trace;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: rrachuma Date: Jul 22, 2008 Time: 12:41:05 PM To change this
 * template use File | Settings | File Templates.
 */
public class LBWeight extends LBBase {
  private static Trace Log = Trace.getTrace(LBWeight.class.getName());

  private static final SecureRandom randomGenerator = new SecureRandom();

  private static final SecureRandom zeroWeightRandomGenerator = new SecureRandom();

  protected void setKey() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  protected ServerGroupElementInterface selectElement() {
    if (Log.on && Log.isTraceEnabled()) {
      Log.trace("entering selectElement()");
    }
    ServerGroupElementInterface selectedElement = null;
    ArrayList list = new ArrayList();
    float[] weightRanges = new float[domainsToTry.size()];
    ArrayList zeroWeightIndices = null;
    float highestQ = -1;
    int totalWeight = 0;
    ServerGroupElementInterface sge;
    int index = 0;
    for (Object o : domainsToTry) {
      sge = (ServerGroupElementInterface) o;
      if (Float.compare(highestQ, -1) == 0) {
        highestQ = sge.getQValue();
        list.add(sge);
        if (sge.getWeight() > 0) {
          totalWeight = totalWeight + sge.getWeight();
          weightRanges[index++] = totalWeight;
        } else if (sge.getWeight() == 0) {
          weightRanges[index] = totalWeight;

          if (zeroWeightIndices == null) zeroWeightIndices = new ArrayList();

          zeroWeightIndices.add(new Integer(index));
          index++;
        }

      } else if (Float.compare(sge.getQValue(), highestQ) == 0) {
        list.add(sge);
        if (sge.getWeight() > 0) {
          totalWeight = totalWeight + sge.getWeight();
          weightRanges[index++] = totalWeight;
        } else if (sge.getWeight() == 0) {
          weightRanges[index] = totalWeight;

          if (zeroWeightIndices == null) zeroWeightIndices = new ArrayList();

          zeroWeightIndices.add(new Integer(index));
          index++;
        }
      } else break;
    }
    if (Log.on && Log.isInfoEnabled()) {
      StringBuffer output = new StringBuffer();
      for (Object o : list) {
        output.append(o.toString());
        output.append(", ");
      }
      Log.info("list of elements in order on which load balancing is done : " + output);
    }

    if (list.size() == 1 && zeroWeightIndices == null) {
      selectedElement = (ServerGroupElementInterface) list.get(0);
    } else {
      float random = randomGenerator.nextFloat();
      if ((Float.compare(random, 0.0f) == 0) && zeroWeightIndices != null) {
        int zeroIndex = 0;
        if (zeroWeightIndices.size() > 1) {
          zeroIndex = zeroWeightRandomGenerator.nextInt(zeroWeightIndices.size());
        }
        selectedElement =
            (ServerGroupElementInterface)
                list.get(((Integer) zeroWeightIndices.get(zeroIndex)).intValue());
      } else if (totalWeight != 0) {
        random = random * (float) totalWeight;
        for (int j = 0; j < list.size(); j++) {
          if (weightRanges[j] >= random) {
            selectedElement = (ServerGroupElementInterface) list.get(j);
            break;
          }
        }
      }
    }

    if (Log.on && Log.isTraceEnabled()) {
      Log.trace("Leaving selectElement, selected element: " + selectedElement);
    }
    return selectedElement;
  }
}
