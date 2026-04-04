import React, { useState, useEffect, useRef } from 'react';
import { AutoComplete, Input } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { searchApi } from '../api/search';
import type { SearchResults } from '../api/search';

const GlobalSearch: React.FC = () => {
  const [value, setValue] = useState('');
  const [options, setOptions] = useState<any[]>([]);
  const navigate = useNavigate();
  const urlMapRef = useRef<Record<string, string>>({});

  useEffect(() => {
    if (!value.trim()) {
      setOptions([]);
      return;
    }

    const timer = setTimeout(() => {
      searchApi
        .search(value.trim())
        .then((data: SearchResults) => {
          const newUrlMap: Record<string, string> = {};
          const grouped = Object.entries(data).map(([category, items]) => ({
            label: (
              <span style={{ fontWeight: 600, fontSize: 13, color: '#8c8c8c' }}>
                {category}
              </span>
            ),
            options: items.map((item) => {
              const key = `${category}-${item.id}`;
              newUrlMap[key] = item.url;
              return {
                value: key,
                label: (
                  <div>
                    <div style={{ fontWeight: 600 }}>{item.title}</div>
                    <div style={{ fontSize: 12, color: '#8c8c8c' }}>
                      {item.description}
                    </div>
                  </div>
                ),
              };
            }),
          }));
          urlMapRef.current = newUrlMap;
          setOptions(grouped);
        })
        .catch(() => {
          setOptions([]);
        });
    }, 300);

    return () => clearTimeout(timer);
  }, [value]);

  const handleSelect = (selected: string) => {
    const url = urlMapRef.current[selected];
    if (url) {
      navigate(url);
    }
    setValue('');
    setOptions([]);
  };

  return (
    <AutoComplete
      style={{ width: '100%' }}
      options={options}
      onSelect={handleSelect}
      onSearch={setValue}
      value={value}
    >
      <Input
        prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
        placeholder="Search products, suppliers, orders..."
        allowClear
        variant="filled"
        style={{ borderRadius: 20 }}
      />
    </AutoComplete>
  );
};

export default GlobalSearch;
