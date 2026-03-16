import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { fetchHomeData } from "../services/homeApi";

interface Candidate {
  name: string;
  image: string;
  payout: string;
  odds: string;
  color: string;
}

interface MarketCardData {
  title: string;
  candidates: Candidate[];
  volume: string;
  marketCount: string;
  newsText: string;
  currentIndex: number;
  totalCards: number;
}

interface InfoCardData {
  id: string;
  title: string;
  description: string;
}

interface Option {
  label: string;
  payout: string;
  odds: string;
  color: string;
}

interface TopicCardData {
  id: string;
  category: string;
  title: string;
  date: string;
  options: Option[];
  volume: string;
  marketCount: string;
}

interface TopicSection {
  id: string;
  heading: string;
  cards: TopicCardData[];
}

interface HomeState {
  marketCard: MarketCardData;
  infoCards: InfoCardData[];
  topicSections: TopicSection[];
  loading: boolean;
  error: string | null;
}

const initialState: HomeState = {
  marketCard: {
    title: "",
    candidates: [],
    volume: "",
    marketCount: "",
    newsText: "",
    currentIndex: 1,
    totalCards: 1,
  },
  infoCards: [],
  topicSections: [],
  loading: false,
  error: null,
};

export const loadHomeData = createAsyncThunk("home/loadHomeData", fetchHomeData);

const homeSlice = createSlice({
  name: "home",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(loadHomeData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loadHomeData.fulfilled, (state, action) => {
        state.loading = false;
        state.marketCard = action.payload.marketCard;
        state.infoCards = action.payload.infoCards;
        state.topicSections = action.payload.topicSections;
      })
      .addCase(loadHomeData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? "Failed to load data";
      });
  },
});

export default homeSlice.reducer;
