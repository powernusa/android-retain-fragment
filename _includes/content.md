
#Table of Contents:

  1. [Introduction](/#Introduction)
  2. [Retain Fragment](/#RetainFragment)
  3. [Container Fragment](/#ContainerFragment)

----

<h1 id="Introduction"><b>1. Introduction</b></h1>

###**Why Retain Fragments?**

By default, when an Android application changes configuration during runtime (e.g. [orientation or screen size][1]) the running Actvity is recreated. [This means][2]:

  1. The *current* Activity is destroyed with `#onDestroy()`.
  2. A new instance of the Activity instatiated, and the old one is released for garbage collection.
  3. The *new* Activity is created with `#onCreate()`. 

However, this process creates a problem for developers -- from the [Android docs][3]:

>If restarting your activity requires that you recover large sets of data, re-establish a network connection, or perform other intensive operations, then a full restart due to a configuration change might be a slow user experience. Also, it might not be possible for you to completely restore your activity state with the Bundle that the system saves for you with the onSaveInstanceState() callback — it is not designed to carry large objects (such as bitmaps) and the data within it must be serialized then deserialized, which can consume a lot of memory and make the configuration change slow.

So what do the docs recommend?

>In such a situation, you can alleviate the burden of reinitializing your activity by retaining a Fragment when your activity is restarted due to a configuration change. This fragment can contain references to stateful objects that you want to retain.

###**Issues with Retain Fragments**

Retaining a Fragment can be as simple as calling `#setRetainInstance(true)` inside the `#onCreate()` method a Fragment. 

However, I wouldn't advice using the above approach directly in Fragments that represent parts of your UI because:
  
  1. retained Fragments cannot be placed in the backstack of a FragmentManager.
  2. during configuration changes, `onDestroyView()` and `onCreateView()` are stilled called. Thus, one must still be careful when referencing Views from background tasks, as they might reference invalid or null Views after a configuration change.

The solution is to retain a headless-Fragment (one without UI components) in the FragmentManager.

###**Goals of this Post**

Our goal in the next sections will be:
    
  - Create a Generic `RetainFragment<T>` that can be used to store any type of data.
  - Create a Generic `ContainFragment<T>` that contains the `RetainFragment<T>`, and handles it's lifecycle events (other reasons will be discussed later).
  - Show an example for how to properly use `ContainerFragment<T>` in an application.

<blockquote class="note">
    <h4 class="note"><b>Note:</b></h4> 
    <p>While there are other ways to save a reference to stateful objects other than retain Fragments, e.g. declaring variables in the <code>Application</code> class (or other Singleton object), this is ill advised because it keeps references around for the entire lifetime of the application. </p>

    <p>The goal of <code>RetainFragment</code> should be to keep the reference around for as long as the Fragment needs it, but no longer. This way memory is conserved in the most efficient way possible. </p>
</blockquote>

[1]: http://developer.android.com/guide/topics/manifest/activity-element.html
[2]: http://developer.android.com/guide/topics/resources/runtime-changes.html
[3]: http://developer.android.com/guide/topics/resources/runtime-changes.html#RetainingAnObject
[4]: http://developer.android.com/reference/android/app/Activity.html#onSaveInstanceState(android.os.Bundle)

-----

<h1 id="RetainFragment"><b>2. RetainFragment</b></h1>

The `RetainFragment` class has 4 responsibilities:
  
  1. Store a generic data type, `T`
  2. Stay alive across configuration changes (`setRetainInstance(true)`
  3. Convenience Method: find/create in the `FragmentManager`,
  4. Convenience Method: remove from the `FragmentManager`

{% highlight java %}
public class RetainFragment<T> extends Fragment {
    public T data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keeps this Fragment alive during configuration changes
        setRetainInstance(true);
    }
    
    // Find/Create in FragmentManager
    public static <T> RetainFragment<T> findOrCreate(FragmentManager fm, String tag) {
        RetainFragment<T> retainFragment = (RetainFragment<T>) fm.findFragmentByTag(tag);

        if(retainFragment == null){
            retainFragment = new RetainFragment<>();
            fm.beginTransaction()
                    .add(retainFragment, tag)
                    .commitAllowingStateLoss();
        }

        return retainFragment;
    }

    // Remove from FragmentManager
    public void remove(FragmentManager fm) {
        if(!fm.isDestroyed()){
            fm.beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
            data = null;
        }
    }
}
{% endhighlight %}

-----

<h1 id="ContainerFragment"><b>3. ContainerFragment</b></h1>

The ContainerFragment maintains an up-to-date reference to the RetainFragment in onViewCreated() by using `RetainFragment#findOrCreate()` -- the ContainerFragment is destroyed on configuration changes but the RetainFragment will survive inside of the FragmentManager. 

In addition, the ContainerFragment keeps track of whether the system was destroyed by the Android OS or the user pressing the back button. If the user presses the back button, this indicates that the user is done with the Container so the RetainFragment resources should be freed. However, if the Container is destroyed by the Android OS, it means that it will eventually need to be recreated (e.g. a configuration change), so the RetainFragment resources should not be destroyed.

{% highlight java %}
public abstract class ContainerFragment<T> extends Fragment
{
    private RetainFragment<T> retainFragment;

    // Used as a unique tag (as long as not using multiple ContainerFragments with same type)
    private String tag = getClass().getCanonicalName();

    // Keeps track if this Fragment is being destroy by System or User
    protected boolean destroyedBySystem;

    public ContainerFragment() {}

    // Convenience method to get data.
    public T getData(){ return retainFragment.data; }

    // Convenience method to set data.
    public void setData(T data){ retainFragment.data = data; }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Find or Create a RetainFragment to hold the component
        retainFragment = RetainFragment.findOrCreate(getFragManager(), tag);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset this variable
        destroyedBySystem = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        destroyedBySystem = true;
    }

    @Override
    public void onDestroy() {
        if(destroyedBySystem) onDestroyBySystem(); else onDestroyByUser();
        super.onDestroy();
    }

    // Activity destroyed By User. Perform cleanup of retain fragment.
    public void onDestroyByUser(){
        retainFragment.remove(getFragManager());
        retainFragment.data = null;
        retainFragment = null;
    }

    // Activity destroyed by System. Subclasses can override this if needed.
    public void onDestroyBySystem(){}

    public FragmentManager getFragManager(){
        return getActivity().getSupportFragmentManager();
    }
}
{% endhighlight %}