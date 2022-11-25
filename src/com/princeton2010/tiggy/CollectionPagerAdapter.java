package com.princeton2010.tiggy;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

//View stuff
//Since this is an object collection, use a FragmentStatePagerAdapter,
//and NOT a FragmentPagerAdapter.
public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
	
	//ids for our fragments; add to this list if you want more panels
	public static enum frag_type { FRAG_MAP, FRAG_MUSIC };
	
	//the actual fragments for each panel
	public MapFragment mapFrag;
	public MusicFragment musicFrag;
	
	//reference to the pager that controls transitions between panels
	private ViewPager mPager;
	
	//last panel that has been displayed
	private int lastItem;
	
	public CollectionPagerAdapter(FragmentManager fm) {
		super(fm);
		mapFrag = new MapFragment();
		musicFrag = new MusicFragment();
		lastItem = -1;
	}
	
	public Fragment getItem(frag_type i) {
		return getItem(i.ordinal());
	}
	
	@Override
	public Fragment getItem(int i) {
		
		if (i == CollectionPagerAdapter.frag_type.FRAG_MAP.ordinal()) {
			  return mapFrag;
		} else if (i == CollectionPagerAdapter.frag_type.FRAG_MUSIC.ordinal()) {
			return musicFrag;
		} else {
			throw new IllegalArgumentException("Invalid Fragment identifier: " + i);
		}

	}
	
	public void setPager(ViewPager p) {
		this.mPager = p;
	}
	
	//update when certain pages get focus
	@Override
	public void finishUpdate(ViewGroup v) {
		
		super.finishUpdate(v);
		
		//the last item things is needed for the map fragment, which will continually update itself if we don't have this check here
		if (mPager.getCurrentItem() != lastItem) {
			if (mPager.getCurrentItem() == frag_type.FRAG_MUSIC.ordinal()) {
				musicFrag.update();
			} else if (mPager.getCurrentItem() == frag_type.FRAG_MAP.ordinal()) {
				mapFrag.toastIfNoInternet();
			}
		}
		lastItem = mPager.getCurrentItem();
	}
	
	@Override
	public int getCount() {
		return frag_type.values().length;
	}
	
	@Override
	public CharSequence getPageTitle(int i) {
	
		if (i == CollectionPagerAdapter.frag_type.FRAG_MAP.ordinal()) {
			  return "Map";
		} else if (i == CollectionPagerAdapter.frag_type.FRAG_MUSIC.ordinal()) {
			return "Music";
		} else {
			throw new IllegalArgumentException("Invalid Fragment identifier: " + i);
		}
		
	}
	
}