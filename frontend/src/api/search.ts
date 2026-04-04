import apiClient from './client';

export interface SearchResult {
  id: number;
  title: string;
  description: string;
  url: string;
}

export type SearchResults = Record<string, SearchResult[]>;

export const searchApi = {
  search: (q: string) =>
    apiClient.get<SearchResults>('/search', { params: { q } }).then(r => r.data),
};
