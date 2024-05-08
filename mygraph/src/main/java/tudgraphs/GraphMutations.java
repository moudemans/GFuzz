package tudgraphs;/*
 * Created by Melchior Oudemans for the bachelors research project at the TUDelft. Adopted for master thesis project at the TU Delft.
 */

import java.util.ArrayList;
import java.util.Random;


public class GraphMutations {

    // Indicates whether a mutation is active or not
    public static final boolean noMutationActive = false;  // Boolean is used to generate mutation list that can be applied. NoMutation is an indication that no Graphs.Mutation can be performed and does not match a specific mutation approach
    public static final boolean copySubsetActive = false;

    public static  boolean addEdgeActive = true;
    public static  boolean removeEdgeActive = true;
    public static  boolean changeEdgeLabelActive = true;

    public static  boolean addNodeActive = true;
    public static  boolean removeNodeActive = true;
    public static  boolean copyNodeActive = true;

    public static  boolean changePropertyValueActive = true;
    public static  boolean removePropertyActive = true;
    public static  boolean addPropertyActive = false;
    public static  boolean changePropertyTypeActive = false;

    public static  boolean breakSchemaActive = true;
    public static  boolean breakCardinalityActive = false;
    public static  boolean breakUniqueActive = false;
    public static  boolean breakNullActive = false;
    public static  boolean bitMutationActive = false;
    public static  boolean byteMutationActive = false;
    private static ArrayList<MutationMethod> activeMutations;

    // Indicates a bias towards the mutation method
    private static final boolean biasEnabled = false;
    private static final float copySubsetBias = 1f;
    private static final float addNodeBias = 1f;
    private static final float removeNodeBias = 1f;
    private static final float copyNodeBias = 1f;
    private static final float addEdgeBias = 1f;
    private static final float removeEdgeBias = 1f;
    private static final float changeEdgeLabelBias = 1f;
    private static final float changePropertyValueBias = 1f;
    private static final float addPropertyBias = 1f;
    private static final float removePropertyBias = 1f;
    private static final float changePropertyTypeBias = 1f;
    private static final float insertCycleBias = 1f;
    private static final float breakCardinalityBias = 1f;
    private static final float breakUniqueBias = 1f;
    private static final float breakNullBias = 1f;
    private static final float breakSchemaBias = 1f;
    private static final float bitBias = 1f;
    private static final float byteBias = 1f;

    /**
     * List of mutations. Indicated with corresponding label from BigFuzz paper when applicable.
     */
    public enum MutationMethod {
        NoMutation,
        CopySubset,    // Select subset of nodes and copy them
        AddNode,
        RemoveNode,
        CopyNode,

        AddEdge,
        RemoveEdge,
        ChangeLabelEdge,

        ChangePropertyValue,
        RemoveProperty,
        AddProperty,
        ChangePropertyType,
        BreakSchema,
        BreakCardinality,
        BreakUnique,
        BreakNull,
        BitMutation,
        ByteMutation
    }

    /**
     * Return the defined bias of the passed MutationMethod
     *
     * @param mutationMethod MutationMethod from which the bias should be selected
     * @return float containing the bias of the method
     */
    private static float getMutationMethodBias(MutationMethod mutationMethod) {
        switch (mutationMethod) {
            case NoMutation -> {
                return 1f;
            }
            case CopySubset -> {
                return copySubsetBias;
            }
            case AddNode -> {
                return addNodeBias;
            }
            case RemoveNode -> {
                return removeNodeBias;
            }
            case CopyNode -> {
                return copyNodeBias;
            }
            case AddEdge -> {
                return addEdgeBias;
            }
            case RemoveEdge -> {
                return removeEdgeBias;
            }
            case ChangeLabelEdge -> {
                return changeEdgeLabelBias;
            }
            case ChangePropertyValue -> {
                return changePropertyValueBias;
            }
            case RemoveProperty -> {
                return removePropertyBias;
            }
            case AddProperty -> {
                return addPropertyBias;
            }
            case ChangePropertyType -> {
                return changePropertyTypeBias;
            }
            case BreakCardinality -> {
                return breakCardinalityBias;
            }
            case BreakUnique -> {
                return breakUniqueBias;
            }
            case BreakNull -> {
                return breakNullBias;
            }
            case BreakSchema -> {
                return breakSchemaBias;
            }
            case BitMutation -> {
                return bitBias;
            }
            case ByteMutation -> {
                return byteBias;
            }
            default -> {
                System.out.println("No Mutation bias defined for [" + mutationMethod + "], returning default of 1");
                return 1f;
            }
        }
    }

    /**
     * Instantiate the list of activeMutations using the mutation activity booleans defined in this class.
     */
    private static ArrayList<MutationMethod> createActiveMutations() {
        ArrayList<MutationMethod> holder = new ArrayList<>();

        // For Every mutation defined in the enum. Check if the mutation has been enables and ad it to the mutation list
        for (MutationMethod h :
                MutationMethod.values()) {
            if (isMutationActive(h)) {
                holder.add(h);
            }
        }

        return holder;
    }

    private static boolean isMutationActive(MutationMethod m) {
        switch (m) {
            case NoMutation:
                return false;
            case CopySubset:
                return copySubsetActive;
            case AddNode:
                return addNodeActive;
            case RemoveNode:
                return removeNodeActive;
            case CopyNode:
                return copyNodeActive;
            case AddEdge:
                return addEdgeActive;
            case RemoveEdge:
                return removeEdgeActive;
            case ChangeLabelEdge:
                return changeEdgeLabelActive;
            case ChangePropertyValue:
                return changePropertyValueActive;
            case AddProperty:
                return addPropertyActive;
            case RemoveProperty:
                return removePropertyActive;
            case ChangePropertyType:
                return changePropertyTypeActive;
            case BreakCardinality:
                return breakCardinalityActive;
            case BreakUnique:
                return breakUniqueActive;
            case BreakNull:
                return breakNullActive;
            case BreakSchema:
                return breakSchemaActive;
            case BitMutation:
                return bitMutationActive;
            case ByteMutation:
                return byteMutationActive;
            default:
                System.out.println("No activation boolean defined for: " + m);
                return false;
        }

    }


    /**
     * Get random active mutation.
     *
     * @param r Random object from which should be used to randomized (can be left null)
     * @return a random active mutation
     */
    public static MutationMethod getRandomMutation(Random r) {
        if (r == null) {
            r = new Random();
        }

        return selectWeightedMutation(r);
    }

    /**
     * Select a random mutation depending on the bias defined in the class
     *
     * @param r Random object from which the selection should be done
     * @return a random weighted mutation
     */
    private static MutationMethod selectWeightedMutation(Random r) {
        ArrayList<MutationMethod> mutationList = getActiveMutationMethodList();

        // If bias is not enabled, return a random index of the mutation list (for performance reasons)
        if (!biasEnabled) {
            return mutationList.get(r.nextInt(mutationList.size()));
        }

        float[] biasWeights = new float[mutationList.size()];
        float total = 0;
        for (int i = 0; i < mutationList.size(); i++) {
            float f = getMutationMethodBias(mutationList.get(i));
            biasWeights[i] = f;
            total += f;
        }

        // If there is no weight found, either all mutations are set to 0 or the passed mutation list is empty. No mutation can be selected
        if (total == 0) {
            return MutationMethod.NoMutation;
        }

        float randomFloatMultiple = r.nextFloat();
        float randomFloat = randomFloatMultiple * total;
        float sumFloat = 0;
        for (int i = 0; i < mutationList.size(); i++) {
            sumFloat += biasWeights[i];
            if (sumFloat >= randomFloat) {
                return mutationList.get(i);
            }
        }

        //Code should not reach this part
        printString("Something went wrong in selecting a weighted mutation: " + mutationList, false);
        return MutationMethod.NoMutation;
    }


    /**
     * Returns a list of (active) mutations that can be applied to input.
     *
     * @return list of active mutations
     */
    public static ArrayList<MutationMethod> getActiveMutationMethodList() {
        if (activeMutations == null) {
            activeMutations = createActiveMutations();
            // Graphs.Mutation active status does not change once the program started. When first time called, create the active mutation list
            if (activeMutations.isEmpty()) {
                printString("No active mutations found", false);
                System.exit(0);
            }
        }


        return activeMutations;
    }


    /**
     * Returns a MutationMethod depending on the passed parameter:
     * 0:
     *
     * @param i integer corresponding to a MutationMethod
     * @return MutationMethod matching the passed integer i
     */
    public static MutationMethod intToMutationMethod(int i) {
        MutationMethod[] m_array = MutationMethod.values();
        if (i > m_array.length || i < 0) {
            printString("Int for MutationMethod not matching a Mutation, int: [" + i + "]", false);
        }
        return m_array[i];
    }

    /**
     * Returns an int depending on the Mutation method given.
     *
     * @param m MutationMethod
     * @return Integer that matched the passed MutationMethod
     */
    public static int MutationMethodToInt(MutationMethod m) {
        MutationMethod[] m_array = MutationMethod.values();
        for (int i = 0; i < m_array.length; i++) {
            if (m == m_array[i]) {
                return i;
            }
        }
        printString("Mutation method not recognised", false);
        return 0;
    }

    public static void printString(String s, boolean isInfo) {
        if (false) {
            System.out.println(s);
        }
    }
}
