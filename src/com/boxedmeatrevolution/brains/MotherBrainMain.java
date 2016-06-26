package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by aidan on 2016-06-25.
 */
public class MotherBrainMain {

    public static final int NUM_BRAINS = 4;
    public static final int NUM_ELEMENTS = 1000000;
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket server = new ServerSocket(9122);
        Socket[] sockets = new Socket[NUM_BRAINS];
        for (int i = 0; i < NUM_BRAINS; ++i) {
            sockets[i] = server.accept();
            System.out.println((i + 1) + " brains have connected");
        }
        
        MotherBrain motherBrain = new MotherBrain(Arrays.asList(sockets));
        
        List<Integer> numbers = new ArrayList<>(NUM_ELEMENTS);
        Random random = new Random();
        for (int i = 0; i < NUM_ELEMENTS; ++i) {
            numbers.add(random.nextInt());
        }
        
        final List<List<Integer>> subLists = new ArrayList<List<Integer>>(NUM_BRAINS);
        for (int i = 0; i < NUM_BRAINS; ++i) {
            int divisor = NUM_ELEMENTS / NUM_BRAINS;
            int start = i * divisor;
            int end = (i + 1) * divisor;
            subLists.add(numbers.subList(start, end));
        }
        
        for (int i = 0; i < NUM_BRAINS; ++i) {
            final int index = i;
            motherBrain.dispatch(
                new ListSorterTask<>(subLists.get(i)),
                new MotherBrain.TaskFinishedEventListener() {
                    @Override
                    public synchronized void onTaskFinished(Serializable result) {
                        subLists.set(index, (List<Integer>) result);
                        ++_numComplete;
                        System.out.println("BRAIN DOZNO!!!!, NumComplete " + _numComplete);
                    }
                });
        }
        
        while (_numComplete != NUM_BRAINS) { }
        
        System.out.println("Finished sorting, merging....");
        
        int[] assembledList = new int[NUM_ELEMENTS];
        int listPosition = NUM_ELEMENTS - 1;
        do {
            int largest = Integer.MIN_VALUE;
            int largestIndex = -1;
            for (int i = 0; i < NUM_BRAINS; ++i) {
                List<Integer> nextList = subLists.get(i);
                if (nextList.isEmpty()) {
                    continue;
                }
                int next = nextList.get(nextList.size() - 1);
                if (next > largest) {
                    largest = next;
                    largestIndex = i;
                }
            }
            if (largestIndex == -1) {
                break;
            }
            assembledList[listPosition] = largest;
            --listPosition;
            subLists.get(largestIndex).remove(subLists.get(largestIndex).size() - 1);
        } while (true);
        
        
        
        System.out.println(Arrays.toString(assembledList));
    }
    
    private static volatile int _numComplete = 0;

}
