import { Empty, Typography } from 'antd';
const { Text } = Typography;

interface EmptyStateProps {
  description?: string;
  icon?: React.ReactNode;
}

export default function EmptyState({ description = 'No data found', icon }: EmptyStateProps) {
  return (
    <div style={{ textAlign: 'center', padding: '60px 0' }}>
      {icon || <Empty description={false} />}
      <Text type="secondary" style={{ display: 'block', marginTop: 16, fontSize: 15 }}>
        {description}
      </Text>
    </div>
  );
}
