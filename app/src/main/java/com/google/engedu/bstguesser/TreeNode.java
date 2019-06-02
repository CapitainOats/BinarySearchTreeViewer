/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.bstguesser;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class TreeNode {
    private static final int SIZE = 60;
    private static final int MARGIN = 20;
    private int value, height;
    protected TreeNode left, right;
    // You can (temporarily) change the logic in draw to always show the value of a node
    // to verify that you are correctly inserting the values into the tree.
    private boolean showValue;
    private int x, y;
    private int color = Color.rgb(150, 150, 250);

    public TreeNode(int value) {
        this.value = value;
        this.height = 0;
        showValue = false;
        left = null;
        right = null;
    }

    /* Values smaller than a node must be stored in its left descendants and values greater than
      a node must be stored in its right descendants.
      This method always return root node.
      */
    public TreeNode insert(TreeNode currentNode, int valueToInsert) {
        if (currentNode == null) {
            return new TreeNode(valueToInsert);
        }

        if (valueToInsert < currentNode.value) {
            currentNode.left = insert(currentNode.left, valueToInsert);
        } else if (valueToInsert > currentNode.value) {
            currentNode.right = insert(currentNode.right, valueToInsert);
        }

        currentNode = balanceTree(currentNode, valueToInsert);

        currentNode.height = calculateTreeHeight(currentNode);

        return currentNode;
    }

    // Get balance factor of TreeNode.
    public int getBalance(TreeNode node) {
        if (node == null) {
            return 0;
        }
        return height(node.left) - height(node.right);
    }

    public int getValue() {
        return value;
    }

    public void positionSelf(int x0, int x1, int y) {
        this.y = y;
        x = (x0 + x1) / 2;

        if(left != null) {
            left.positionSelf(x0, right == null ? x1 - 2 * MARGIN : x, y + SIZE + MARGIN);
        }
        if (right != null) {
            right.positionSelf(left == null ? x0 + 2 * MARGIN : x, x1, y + SIZE + MARGIN);
        }
    }

    public void draw(Canvas c) {
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3);
        linePaint.setColor(Color.GRAY);
        if (left != null)
            c.drawLine(x, y + SIZE/2, left.x, left.y + SIZE/2, linePaint);
        if (right != null)
            c.drawLine(x, y + SIZE/2, right.x, right.y + SIZE/2, linePaint);

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(color);
        c.drawRect(x-SIZE/2, y, x+SIZE/2, y+SIZE, fillPaint);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(SIZE * 2/3);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(showValue ? String.valueOf(value) : "?", x, y + SIZE * 3/4, paint);

        if (height > 0) {
            Paint heightPaint = new Paint();
            heightPaint.setColor(Color.MAGENTA);
            heightPaint.setTextSize(SIZE * 2 / 3);
            heightPaint.setTextAlign(Paint.Align.LEFT);
            c.drawText(String.valueOf(height), x + SIZE / 2 + 10, y + SIZE * 3 / 4, heightPaint);
        }

        if (left != null)
            left.draw(c);
        if (right != null)
            right.draw(c);
    }

    public int click(float clickX, float clickY, int target) {
        int hit = -1;
        if (Math.abs(x - clickX) <= (SIZE / 2) && y <= clickY && clickY <= y + SIZE) {
            if (!showValue) {
                if (target != value) {
                    color = Color.RED;
                } else {
                    color = Color.GREEN;
                }
            }
            showValue = true;
            hit = value;
        }
        if (left != null && hit == -1)
            hit = left.click(clickX, clickY, target);
        if (right != null && hit == -1)
            hit = right.click(clickX, clickY, target);
        return hit;
    }

    public void invalidate() {
        color = Color.CYAN;
        showValue = true;
    }

    private TreeNode balanceTree(TreeNode currentNode, int valueToInsert) {
        int balance = getBalance(currentNode);

        // Right-right case.
        if (balance < -1 && valueToInsert > currentNode.right.value) {
            return leftRotate(currentNode);
        }

        // Left-left case.
        if (balance > 1 && valueToInsert < currentNode.left.value) {
            return rightRotate(currentNode);
        }

        // Left-right case.
        if (balance > 1 && valueToInsert > currentNode.left.value) {
            currentNode.left = leftRotate(currentNode.left);
            return rightRotate(currentNode);
        }

        // Right-left case.
        if (balance < -1 && valueToInsert < currentNode.right.value) {
            currentNode.right = rightRotate(currentNode.right);
            return leftRotate(currentNode);
        }

        return currentNode;

    }

    private int height(TreeNode currentNode) {
        if (currentNode == null) {
            return -1;
        }
        return currentNode.height;
    }

    private int calculateTreeHeight(TreeNode currentNode) {
        return Math.max(height(currentNode.left), height(currentNode.right)) + 1;
    }

    private TreeNode leftRotate(TreeNode currentNode) {
        TreeNode newRootNode = currentNode.right;
        TreeNode leftChildOfRight = newRootNode.left;

        // Step 1. set the left child of the new root node
        newRootNode.left = currentNode;

        // Step 2. set the right child of the new left child
        currentNode.right = leftChildOfRight;

        // Step 3. Update the height of the trees that were updated.
        newRootNode.height = calculateTreeHeight(newRootNode);
        currentNode.height = calculateTreeHeight(currentNode);

        return newRootNode;
    }

    private TreeNode rightRotate(TreeNode currentNode) {
        TreeNode newRootNode = currentNode.left;
        TreeNode rightChildOfLeft = newRootNode.right;

        // Step 1. Set newRootNode as the new root node.
        newRootNode.right = (currentNode);

        // Step 2. Set the right child of the new left child of the new root node. Quite a tongue twister right?
        currentNode.left = (rightChildOfLeft);

        // Step 3. Update the height of the trees that were updated.
        newRootNode.height = (calculateTreeHeight(newRootNode));
        currentNode.height = (calculateTreeHeight(currentNode));

        return newRootNode;
    }
}
