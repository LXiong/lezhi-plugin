#cqlsh -2
use lezhi_plugin;
ALTER COLUMNFAMILY site_config ADD width int;
ALTER COLUMNFAMILY site_config ADD height int;
ALTER COLUMNFAMILY site_config ADD titleBgColor text;
ALTER COLUMNFAMILY site_config ADD titleImage text;
ALTER COLUMNFAMILY site_config ADD titleFontSize int;
ALTER COLUMNFAMILY site_config ADD titleBold boolean;
ALTER COLUMNFAMILY site_config ADD fontBold boolean;
ALTER COLUMNFAMILY site_config ADD fontUnderline boolean;
ALTER COLUMNFAMILY site_config ADD linkUnderline boolean;
ALTER COLUMNFAMILY site_config ADD redirectType text;
ALTER COLUMNFAMILY site_config ADD picMatch text;
ALTER COLUMNFAMILY site_config ADD bgcolor text;
ALTER COLUMNFAMILY site_config ADD picBorderRadius boolean;
ALTER COLUMNFAMILY site_config ADD lineHeight int;
ALTER COLUMNFAMILY site_config ADD listType text;
ALTER COLUMNFAMILY site_config ADD positionY text;

CREATE COLUMN FAMILY exchange_link WITH comparator = UTF8Type AND default_validation_class=UTF8Type;
