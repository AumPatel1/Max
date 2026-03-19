import { createSlice } from "@reduxjs/toolkit";

interface SearchState {
  isSearchOpen: boolean;
  shouldShake: boolean;
}

const initialState: SearchState = {
  isSearchOpen: false,
  shouldShake: false,
};

const searchSlice = createSlice({
  name: "search",
  initialState,
  reducers: {
    toggleSearch(state) {
      state.isSearchOpen = !state.isSearchOpen;
      state.shouldShake = true;
    },
    closeSearch(state) {
      state.isSearchOpen = false;
    },
    triggerShake(state) {
      state.shouldShake = true;
    },
    resetShake(state) {
      state.shouldShake = false;
    },
  },
});

export const { toggleSearch, closeSearch, triggerShake, resetShake } =
  searchSlice.actions;
export default searchSlice.reducer;
