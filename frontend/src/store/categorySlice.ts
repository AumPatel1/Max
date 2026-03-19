import { createSlice } from "@reduxjs/toolkit";

interface CategoryState {
  categories: string[];
}

const initialState: CategoryState = {
  categories: [
    "Trending",
    "Politics",
    "Sports",
    "Culture",
    "Crypto",
    "Climate",
    "Economics",
    "Mentions",
    "Companies",
    "Financials",
    "Tech & Science",
  ],
};

const categorySlice = createSlice({
  name: "category",
  initialState,
  reducers: {},
});

export default categorySlice.reducer;
