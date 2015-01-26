/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;

import javax.annotation.Nonnull;

/**
 * User: Solovyev_S
 * Date: 03.10.12
 * Time: 14:18
 */
public abstract class CalculatorListFragment extends SherlockListFragment {

	@Nonnull
	private final FragmentUi fragmentHelper;

	protected CalculatorListFragment(int layoutResId, int titleResId) {
		fragmentHelper = CalculatorApplication.getInstance().createFragmentHelper(layoutResId, titleResId);
	}

	protected CalculatorListFragment(@Nonnull CalculatorFragmentType fragmentType) {
		fragmentHelper = CalculatorApplication.getInstance().createFragmentHelper(fragmentType.getDefaultLayoutId(), fragmentType.getDefaultTitleResId());
	}

	protected CalculatorListFragment(@Nonnull FragmentUi fragmentHelper) {
		this.fragmentHelper = fragmentHelper;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fragmentHelper.onCreate(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return fragmentHelper.onCreateView(this, inflater, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		fragmentHelper.onViewCreated(this, view);
	}

	@Override
	public void onResume() {
		super.onResume();

		this.fragmentHelper.onResume(this);
	}

	@Override
	public void onPause() {
		this.fragmentHelper.onPause(this);

		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		fragmentHelper.onDestroy(this);
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}

