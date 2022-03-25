/*
 * Copyright (c) 2022. CMPUT301W22T29
 * Subject to MIT License
 * See full terms at https://github.com/CMPUT301W22T29/QuiRky/blob/main/LICENSE
 */

package com.example.quirky;

// This interface is used in PlayerSearchActivity.java, QRAdapter.java

// RecyclerClickerListener code sourced form:
// https://www.tutorialspoint.com/get-clicked-item-and-its-position-in-recyclerview
// Made by:
// https://www.tutorialspoint.com/answers/Nitya-Raut
public interface RecyclerClickerListener {
    void OnClickListItem(int position);
}