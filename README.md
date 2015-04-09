# RecyclerGesture
Lightweight library used to easily attached simple gesture to a recycler view based on RecycleView.addOnItemTouchListener()

* [Drag&Drop](#drag&drop)

#Drag&Drop

Drag&Drop gesture allow you to easily sort items displayed in your RecyclerView.

No need to change or extend RecyclerView.

No need to change or extend RecylerView.Adapter.

No need to replace/remove OnScrollListener if one is used.

##Simple usage
```java
    DragDropGesture.Builder builder = new DragDropGesture.Builder().on(recyclerView).build();
```
In addition your Adapter must implement the DragDropGesture.Swapper interface to perfom the data swapping.

For instance (if your Adapter is based on an ArrayList) :
```java
    @Override
    public void swapPositions(int from, int to) {
        Collections.swap(arrayList, from, to);
    }
```
##Features

###Horizontal RecyclerView
If your RecyclerView is horizontal, thanks to an horizontal LinearLayoutManager for instance, simply add one line to your builder :
```java
    DragDropGesture.Builder builder = new DragDropGesture.Builder()
                .on(recyclerView)
                .horizontal()
                .build();
```
###Dividers
Define your own Drag&Drop strategy in order to customize item "draggability" as well as item "hoverability" :
```java
    /**
     * Dummy strategy used to disable drag on divider.
     */
    private class DummyDragStrategy extends DragStrategy {
        @Override
        public boolean isItemDraggable(int position) {
            DummyModel model = models.get(position);
            return !model.isDivider;
        }

        @Override
        public boolean isItemHoverable(int position) {
            DummyModel model = models.get(position);
            return !model.isDivider;
        }
    }
```

Then just apply the Drag&Drop strategy :
```java
    DragDropGesture.Builder builder = new DragDropGesture.Builder()
                .on(recyclerView)
                .apply(new DummyDragStrategy())
                .build();
```

# Contribution
PR are welcomed (= !

Try to fit the current naming convention.

Ensure that gradlew :library:check succeeded before submitting any PR.

# Restrictions
Currently work with fixedSize item in the recycler view.

```java

    recyclerView.setHasFixedSize(true);

```

Currently don't work with LinearLayout as RecyclerView parent since dragging view is added to the
parent view hierarchy. Works fine with RelativeLayout/FrameLayout.

#Disclaimer
Under development, check restrictions above.

Based on previous work of ismoli :
https://github.com/ismoli/DynamicRecyclerView

