# RecyclerGesture
Lightweight library used to easily attached simple gesture to a recycler view based on RecycleView.addOnItemTouchListener()

* [Drag&Drop](#dragdrop)
* [SwipeToDismiss](#swipetodismiss)

# Restrictions
Currently only works with RecyclerView#hasFixedSize sets to true.

```java

    recyclerView.setHasFixedSize(true);

```

#Drag&Drop

Drag&Drop gesture allow you to easily sort items displayed in your RecyclerView.

No need to change or extend RecyclerView.

No need to change or extend RecyclerView.Adapter.

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

###Listener
Define your own Listener to implement specific behaviour according to drag&drop events:
```java
    /**
     * Dummy drag&drop listener.
     */
    private class DummyListener implements DragDropGesture.Listener{

            @Override
            public void onDragStarted() {

            }

            @Override
            public void onDragEnded() {

            }
        }
```

Then just register the Drag&Drop listener:
```java
    DragDropGesture.Builder builder = new DragDropGesture.Builder()
                .on(recyclerView)
                .register(new DummyListener())
                .build();
```

#SwipeToDismiss

SwipToDimiss gesture allow you to easily sort items displayed in your RecyclerView.

No need to change or extend RecyclerView.

No need to change or extend RecyclerView.Adapter.

No need to replace/remove OnScrollListener if one is used.

##Simple usage
```java
    new SwipeToDismissGesture.Builder(SwipeToDismissDirection.HORIZONTAL).on(recyclerView).build();
```
In addition your Adapter must implement the SwipeToDismissGesture.Dismisser interface to perfom the data deletion.

For instance (if your Adapter is based on an ArrayList) :
```java
    @Override
        public void dismiss(int position) {
            dataList.remove(position);
        }
```

##Features

###Dividers
Define your own SwipeToDismiss strategy in order to customize item "swapability" :
```java
    /**
     * Dummy swipe strategy used to disable swipe on divider.
     */
    private class DummySwipeStrategy extends SwipeToDismissStrategy {

        @Override
        public SwipeToDismissDirection getDismissDirection(int position) {
            DummyModel model = models.get(position);
            if (model.isDivider) {
                return SwipeToDismissDirection.NONE;
            } else {
                return super.getDismissDirection(position);
            }
        }
    }
```

Then just apply the SwipeToDismiss strategy :
```java
    new SwipeToDismissGesture.Builder(SwipeToDismissDirection.HORIZONTAL)
                    .on(recyclerView)
                    .apply(new DummySwipeStrategy())
                    .build();
```


# Contribution
PR are welcomed (= !

Try to fit the current naming convention.

Ensure that gradlew :library:check succeeded before submitting any PR.

# TODO

[SwipeToDismiss] provide undo feature.

Currently don't work with LinearLayout as RecyclerView parent since dragging view is added to the
parent view hierarchy. Works fine with RelativeLayout/FrameLayout.

#Disclaimer
Under development, check restrictions above.

Based on previous work of ismoli :
https://github.com/ismoli/DynamicRecyclerView

